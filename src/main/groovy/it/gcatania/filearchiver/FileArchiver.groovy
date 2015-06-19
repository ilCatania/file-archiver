/*
 * Copyright 2015 Gabriele Catania <gabriele.ctn@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.gcatania.filearchiver;

import groovy.io.FileType

import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.time.FastDateFormat
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * this script will iterate the contents of a given folder and rearrange all found files in subfolders by
 * year and month. it will also zip and delete all month folders except the one for the current month
 * @author gcatania
 */
class FileArchiver
{
    /**
     * zip separator is a slash regardless of platform. If you store paths with backslashes in a zip files
     * under windows and then decompress them under linux, you will get single file names and not
     * subdirectories.
     */
    private static final ZIP_SEPARATOR = '/'
    private static final Logger log = LoggerFactory.getLogger(FileArchiver.class)
    private static final Pattern YEAR_PATTERN = ~/\d{4}/
    private static final Pattern MONTH_PATTERN = ~/\d{2}/
    private static final FastDateFormat DATE_PARSE_FORMAT = FastDateFormat.getInstance('yyyyMMdd')
    private static final FastDateFormat YEAR_FORMAT = FastDateFormat.getInstance('yyyy')
    private static final FastDateFormat MONTH_FORMAT = FastDateFormat.getInstance('MM')
    private static final FastDateFormat DAY_FORMAT = FastDateFormat.getInstance('dd')

    private final List<Pattern> pathPatternsToExclude = []
    private final Date referenceDate

    /**
     * @param referenceDate the reference date
     * @param monthsToKeepUnzipped the number of month directories to keep unzipped with respect to reference date.
     * <dl>
     * <dt>0</dt><dd>always zip all folders</dd>
     * <dt>1</dt><dd>maintain only the current month unzipped (e.g. if reference date is 5 january 2015, don't zip folders ending in <tt>2015/01</tt></dd>
     * <dt>2</dt><dd>maintain the current and previous months unzipped (e.g. if reference date is 5 january 2015, don't zip folders ending in <tt>2015/01</tt> and <tt>2014/12</tt></dd>
     * <dt>...</dt><dd>and so on</dd>
     */
    FileArchiver(Date referenceDate, int monthsToKeepUnzipped)
    {
        this.referenceDate = referenceDate;
        Calendar cal = Calendar.getInstance();
        cal.setTime(referenceDate);
        for (int month in 0..<monthsToKeepUnzipped)
        {
            String formattedYear = YEAR_FORMAT.format(cal);
            String formattedMonth = MONTH_FORMAT.format(cal);
            pathPatternsToExclude.add(Pattern.compile(".*${formattedYear}${File.separator}${formattedMonth}\$"));
            cal.add(Calendar.MONTH, -1);
        }
    }

    FileArchiver()
    {
        this(new Date(), 2)
    }

    static void main(String[] args)
    {
        FileArchiver fa = new FileArchiver();
        args.each(
                { String pathName ->
                    log.info('processing path argument: {}', pathName);
                    fa.handleDirectory(transferDir);
                })
    }

    protected Date extractDate(File f)
    {
        // subclasses might want to override
        return new Date(f.lastModified());
    }

    private void archive(File f)
    {
        File parent = f.parentFile;
        Date fileDate = extractDate(f)
        if(fileDate.after(referenceDate))
        {
            log.debug('Skipping file: {} with date: {} after reference date: {}', [f, fileDate, referenceDate] as Object[]);
            return;
        }
        File yearSubDir = new File(parent, YEAR_FORMAT.format(fileDate));
        File monthSubDir = new File(yearSubDir, MONTH_FORMAT.format(fileDate));
        File daySubDir = new File(monthSubDir, DAY_FORMAT.format(fileDate));
        FileUtils.moveFileToDirectory(f, daySubDir, true);
    }

    private void handleYearDir(File dir)
    {
        dir.eachDirMatch(MONTH_PATTERN,
                { File monthDir ->
                    for(Pattern toExclude in pathPatternsToExclude)
                    {
                        if(toExclude.matcher(monthDir.path).matches())
                        {
                            log.debug('Skipping month directory: {}', monthDir.canonicalPath);
                            return;
                        } else log.warn('Pattern: {}, path: {}', toExclude, monthDir.path)
                    }
                    moveToMonthZipFile(monthDir);
                })
    }

    void handleDirectory(File dir)
    {
        if(YEAR_PATTERN.matcher(dir.name).matches())
        {
            handleYearDir(dir);
        }
        else
        {
            dir.eachFile(FileType.FILES,
                    { archive(it); })
            dir.eachFile(FileType.DIRECTORIES,
                    { handleDirectory(it); })
        }
    }

    private void moveToMonthZipFile(File monthDir)
    {
        File monthZip = new File(monthDir.path + '.zip')
        if(monthZip.exists())
        {
            // TODO should probably add to existing zip file instead
            log.warn('{} already exists, skipping compression of directory', monthZip.canonicalPath)
            return;
        }
        log.debug('Creating zip file: {}', monthZip.canonicalPath)
        FileOutputStream fos = new FileOutputStream(monthZip);
        ZipOutputStream zos = new ZipOutputStream(fos);
        addDirToArchive(zos, monthDir, '');
        zos.close();
        if(monthZip.exists())
        {
            log.trace('Created zip archive: {}', monthZip.canonicalPath)
            FileUtils.deleteDirectory(monthDir);
        }
        else log.warn('File not created: {}', monthZip.canonicalPath);
    }

    private void addDirToArchive(ZipOutputStream zos, File srcFile, String relativePrefix)
    {
        log.debug('Adding directory: {}', srcFile.name);
        for (File f : srcFile.listFiles())
        {
            String relativeName = relativePrefix + f.name
            if (f.isDirectory())
            {
                addDirToArchive(zos, f, relativeName + ZIP_SEPARATOR);
                continue;
            }
            log.debug("\t Adding file: ${f.name}");
            byte[] buffer = new byte[1024];
            FileInputStream fis = new FileInputStream(f);
            zos.putNextEntry(new ZipEntry(relativeName));
            int length;
            while ((length = fis.read(buffer)) > 0)
            {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();
            fis.close();
        }
    }
}
