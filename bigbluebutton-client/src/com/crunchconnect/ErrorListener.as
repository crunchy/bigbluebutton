/**
 * Created by IntelliJ IDEA.
 * User: tjsingleton
 * Date: 2/28/12
 * Time: 3:32 PM
 * To change this template use File | Settings | File Templates.
 */
package com.crunchconnect {
import com.crunchconnect.ErrorReporter;

public class ErrorListener {

    private var reporter:ErrorReporter;
    public function ErrorListener(reporter:ErrorReporter) {
        this.reporter = reporter;
    }

}
}
