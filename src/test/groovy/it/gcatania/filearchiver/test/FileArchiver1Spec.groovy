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
class FileArchiver1Spec extends BaseFileArchiverSpec
{
    protected String getTreePropertiesPath()
    {
        return '/trees/tree1/input.properties'
    }

    def 'test reference date 2015-04-01 and zip after 0 months'()
    {
        given:
        FileArchiver fa = new FileArchiver(new GregorianCalendar(2015, Calendar.APRIL, 1).time, 0)

        when:
        fa.handleDirectory(workingDir);

        then:
        directoryContentsMatch('/trees/tree1/output-2015-04-01-zipAfter0Months.txt')
    }

    def 'test reference date 2015-06-01 and zip after 2 months'()
    {
        given:
        FileArchiver fa = new FileArchiver(new GregorianCalendar(2015, Calendar.JUNE, 1).time, 2)

        when:
        fa.handleDirectory(workingDir);

        then:
        directoryContentsMatch('/trees/tree1/output-2015-06-01-zipAfter2Months.txt')
    }
    def 'test reference date 2015-06-01 and zip after 5 months'()
    {
        given:
        FileArchiver fa = new FileArchiver(new GregorianCalendar(2015, Calendar.JUNE, 1).time, 5)

        when:
        fa.handleDirectory(workingDir);

        then:
        directoryContentsMatch('/trees/tree1/output-2015-06-01-zipAfter2Months.txt')
    }
}
