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
package it.gcatania.filearchiver.test

import groovy.io.FileType

import java.nio.file.Files

import org.apache.commons.lang3.time.FastDateFormat
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import spock.lang.Specification


/**
 * @author gcatania
 * @version $Id$
 */
abstract class BaseFileArchiverSpec extends Specification
{
    private static final FastDateFormat DF = FastDateFormat.getInstance('yyyy-MM-dd')
    private static final Logger log = LoggerFactory.getLogger(BaseFileArchiverSpec.class);

    private final String treePropertiesFile

    protected final File workingDir = Files.createTempDirectory('fileArchiverSpec-').toFile();

    protected abstract String getTreePropertiesPath();

    def setup()
    {
        String treePropertiesPath = getTreePropertiesPath()
        InputStream is = BaseFileArchiverSpec.class.getResourceAsStream(treePropertiesPath)
        Properties properties = new Properties()
        properties.load(is)

        log.debug('Loading tree from: {} into: {}', treePropertiesPath, workingDir)
        properties.each(
                { String filePath, String modifyDateStr ->
                    Date d = DF.parse(modifyDateStr);
                    File toCreate = new File(workingDir, filePath)
                    log.trace('Creating file: {} with modify date: {}', toCreate, d);
                    toCreate.createNewFile();
                    toCreate.lastModified = d.time
                })
    }

    def cleanup()
    {
        //        FileUtils.deleteDirectory(workingDir);
    }

    protected final void directoryContentsMatch(String expectedContentsPath)
    {
        File expectedContentsFile = new File(BaseFileArchiverSpec.class.getResource(expectedContentsPath).toURI())
        String workingPathPrefix = workingDir.canonicalPath + File.separator;
        int canonicalPathPrefixLength = workingPathPrefix.length()
        Map<String, String> expectedPaths = [:]

        List<String> unexpectedPaths = [];

        expectedContentsFile.eachLine(
                { String relPath ->
                    String absPath = workingPathPrefix + relPath
                    expectedPaths.put(absPath, relPath)
                })

        workingDir.eachFileRecurse(FileType.FILES,
                { File f ->
                    String actualPath = f.canonicalPath;
                    String relPath = expectedPaths.remove(actualPath)
                    if(relPath == null)
                    {
                        unexpectedPaths.add(actualPath.substring(canonicalPathPrefixLength));
                    }
                })
        def missingPaths = expectedPaths.values()
        assert missingPaths.empty &  unexpectedPaths.empty
        log.debug('Successfully checked: {} against: {}', workingDir, expectedContentsFile)
    }
}
