package io.byzaneo.initializer.facet;

import com.samskivert.mustache.Mustache;
import org.junit.Test;

import java.io.*;
import java.time.LocalDate;

public class LicenseTest {

    private static final Mustache.Compiler MUSTACHE_LICENSE = Mustache.compiler().withDelims("[ ]");
    private static final String TEMPLATE = "MIT License\n\nCopyright (c) [year] [fullname]\n\nPermission is hereby granted, free of charge, to any person obtaining a copy\nof this software and associated documentation files (the \"Software\"), to deal\nin the Software without restriction, including without limitation the rights\nto use, copy, modify, merge, publish, distribute, sublicense, and/or sell\ncopies of the Software, and to permit persons to whom the Software is\nfurnished to do so, subject to the following conditions:\n\nThe above copyright notice and this permission notice shall be included in all\ncopies or substantial portions of the Software.\n\nTHE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\nIMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\nFITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\nAUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\nLIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\nOUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE\nSOFTWARE.\n";

    @Test
    public void simple() throws IOException {
        try (final Reader reader = new BufferedReader(new StringReader(TEMPLATE));
             final Writer writer = new StringWriter()) {
            MUSTACHE_LICENSE.compile(reader).execute(new LicenseContext("Me"), writer);
            System.out.println(writer.toString());
        }
    }

    static final class LicenseContext {
        final Integer year = LocalDate.now().getYear();
        final String fullname;

        LicenseContext(String fullname) {
            this.fullname = fullname;
        }
    }

}
