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

import it.gcatania.filearchiver.FileArchiver


/**
 * @author gcatania
 * @version $Id$
 *
 */
class FileArchiverNestedDirectorySpec extends BaseFileArchiverSpec
{
    protected String getTreePropertiesPath()
    {
        return '/trees/nested/input.properties'
    }

    def 'test no files zipped'()
    {
        given:
        FileArchiver fa = new FileArchiver(new GregorianCalendar(2016, Calendar.JANUARY, 1).time, 13)

        when:
        fa.handleDirectory(workingDir);

        then:
        directoryContentsMatch('/trees/nested/output-none-zipped.txt')
    }

    def 'test all files zipped'()
    {
        given:
        FileArchiver fa = new FileArchiver(new GregorianCalendar(2016, Calendar.JANUARY, 1).time, 0)

        when:
        fa.handleDirectory(workingDir);

        then:
        directoryContentsMatch('/trees/nested/output-all-zipped.txt')
        zipFileContentsMatch('sent/2015/01.zip', '/trees/nested/zipEntries-sent-2015-01.txt')
        zipFileContentsMatch('received/2015/01.zip', '/trees/nested/zipEntries-received-2015-01.txt')
        zipFileContentsMatch('received/lowPriority/2015/03.zip', '/trees/nested/zipEntries-received-lowPriority-2015-03.txt')
        zipFileContentsMatch('2015/07.zip', '/trees/nested/zipEntries-root-2015-07.txt')
    }
}
