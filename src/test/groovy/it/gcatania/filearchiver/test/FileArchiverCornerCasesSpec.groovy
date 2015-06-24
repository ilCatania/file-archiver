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
/**
 *
 */
package it.gcatania.filearchiver.test

import it.gcatania.filearchiver.FileArchiver


/**
 * @author ilCatania
 *
 */
class FileArchiverCornerCasesSpec extends BaseFileArchiverSpec
{
    protected String getTreePropertiesPath()
    {
        return '/trees/cornercases/input.properties'
    }

    /*
     * appending entries to an existing zip is way too much work in java, so for
     * the sake of keeping the script as simple as possible, we'll just skip
     * zipping files if the corresponding zip already exists
     */
    def 'test file not zipped because zip already exists'()
    {
        given:
        FileArchiver fa = new FileArchiver()

        when:
        fa.handleDirectory(workingDir)

        then:
        directoryContentsMatch('/trees/cornercases/output-not-zipped-already-exists.txt')
    }

    def 'test file not zipped because date is after reference date'()
    {
        given:
        FileArchiver fa = new FileArchiver(new GregorianCalendar(2015, Calendar.FEBRUARY, 2).time, 1)

        when:
        fa.handleDirectory(workingDir)

        then:
        directoryContentsMatch('/trees/cornercases/output-not-zipped-after-referencedate.txt')
    }
}
