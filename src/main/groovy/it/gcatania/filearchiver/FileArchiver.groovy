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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import groovy.io.FileType;


/**
 * this script will iterate the contents of a given folder and rearrange all found files in subfolders by
 * year and month. it will also zip and delete all month folders except the one for the current month
 * @author gcatania
 */
class FileArchiver {
    static final ZIP_SEPARATOR = '/'
    static final Logger log = LoggerFactory.getLogger(FileArchiver.class)
    Pattern yearPattern = ~/\d{4}/
    Pattern monthPattern = ~/\d{2}/
    DateFormat parseFormat = new SimpleDateFormat('yyyyMMdd')
    FastDateFormat yearFormat = FastDateFormat.getInstance('yyyy')
    FastDateFormat monthFormat = FastDateFormat.getInstance('MM')
    FastDateFormat dayFormat = FastDateFormat.getInstance('dd')
    Date referenceDate;
    String currentMonthPath
    
    FileArchiver(Date referenceDate) {
        this.referenceDate = referenceDate
        String currentYearStr = yearFormat.format(referenceDate);
        String currentMonthStr = monthFormat.format(referenceDate);
        currentMonthPath = currentYearStr + File.separator + currentMonthStr;
    }

    static void main(String[] args) {
        FileArchiver fa = new FileArchiver(new Date());
        args.each({ String pathName ->
            log.info('processing path argument: {}', pathName);
            fa.handleDirectory(transferDir);
        })
    }

    protected Date extractDate(File f) {
        // subclasses might want to override
        return new Date(f.lastModified());
    }

    private void archive(File f) {
        File parent = f.parentFile;
        Date refDate = extractDate(f)
        File yearSubDir = new File(parent, yearFormat.format(refDate));
        File monthSubDir = new File(yearSubDir, monthFormat.format(refDate));
        File daySubDir = new File(monthSubDir, dayFormat.format(refDate));
        FileUtils.moveFileToDirectory(f, daySubDir, true);
    }

    private void handleYearDir(File dir) {
        Pattern currentMonthPattern = Pattern.compile(currentMonthPath, Pattern.LITERAL);
        dir.eachDirMatch(monthPattern, { File monthDir ->
            if(monthDir.path.endsWith(currentMonthPath)) {
                log.debug('Skipping current month: {}', monthDir.canonicalPath)
            } else moveToMonthZipFile(monthDir);
        })
    }

    private void handleDirectory(File dir) {
        if(yearPattern.matcher(dir.name).matches()) {
            handleYearDir(dir);
            return;
        }
        dir.eachFile(FileType.FILES, { archive(it); })
        dir.eachFile(FileType.DIRECTORIES, { handleDirectory(it); })
    }

    private void moveToMonthZipFile(File monthDir) {
        File monthZip = new File(monthDir.path + '.zip')
        if(monthZip.exists()) {
            log.warn('{} already exists, skipping compression of directory', monthZip.canonicalPath)
            return;
        }
        log.debug('Creating zip file: {}', monthZip.canonicalPath)
        FileOutputStream fos = new FileOutputStream(monthZip);
        ZipOutputStream zos = new ZipOutputStream(fos);
        addDirToArchive(zos, monthDir, '');
        zos.close();
        if(monthZip.exists()) {
            log.trace('Created zip archive: {}', monthZip.canonicalPath)
            FileUtils.deleteDirectory(monthDir);
        }
        else log.warn('File not created: {}', monthZip.canonicalPath);
    }

    private void addDirToArchive(ZipOutputStream zos, File srcFile, String relativePrefix) {
        log.debug('Adding directory: {}', srcFile.name);
        for (File f : srcFile.listFiles()) {
            String relativeName = relativePrefix + f.name
            if (f.isDirectory()) {
                addDirToArchive(zos, f, relativeName + File.separator);
                continue;
            }
            log.debug("\t Adding file: ${f.name}");
            byte[] buffer = new byte[1024];
            FileInputStream fis = new FileInputStream(f);
            zos.putNextEntry(new ZipEntry(relativeName));
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();
            fis.close();
        }
    }
}
