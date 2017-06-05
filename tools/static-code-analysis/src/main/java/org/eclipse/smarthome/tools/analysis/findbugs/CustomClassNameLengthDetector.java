/**
 * test-driven-detectors4findbugs. Copyright (c) 2011 youDevise, Ltd.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
*/
package org.eclipse.smarthome.tools.analysis.findbugs;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.ba.ClassContext;

/**
 * Detects if the name of the class has exceeded a maximum length
 *
 * @see <a href="">https://github.com/tim-group/test-driven-detectors4findbugs</a>
 *
 */
public class CustomClassNameLengthDetector implements Detector {

    private static final int ARBITRARY_MAX_CLASS_NAME_LENGTH = 50;
    private final BugReporter bugReporter;

    public CustomClassNameLengthDetector(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    @Override
    public void report() {
        // This method is called after all classes to be visited.
        // It should be used by any detectors which accumulate information over all visited classes to generate results.
    }

    @Override
    public void visitClassContext(ClassContext classContext) {
        int classNameLength = classContext.getClassDescriptor().getSimpleName().length();

        if (classNameLength > ARBITRARY_MAX_CLASS_NAME_LENGTH) {
            bugReporter.reportBug(new BugInstance("CLASS_NAME_LENGTH", Priorities.NORMAL_PRIORITY));
        }
    }

}
