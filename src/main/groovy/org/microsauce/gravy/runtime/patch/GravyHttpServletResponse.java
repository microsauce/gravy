package org.microsauce.gravy.runtime.patch;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

public interface GravyHttpServletResponse extends HttpServletResponse {
    public void render(String viewUri, Object model);

    public void renderJson(Object model);

    public void print(String output);

    public void println(String outputStr);

    public void write(Byte[] output);

    public void redirect(String url);

    public Object getOut();

    public void setOut(Object out);

    public PrintWriter getPrinter();

    public void setPrinter(PrintWriter out);
}
