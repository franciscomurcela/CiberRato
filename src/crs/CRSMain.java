import java.io.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class CRSMain {
    public static void main(String[] args) {
        try {
            CharStream input;
            if (args.length > 0) {
                input = CharStreams.fromFileName(args[0]);
            } else {
                input = CharStreams.fromStream(System.in);
            }
            crsLexer lexer = new crsLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            crsParser parser = new crsParser(tokens);
            ParseTree tree = parser.scene();
            CRSCompiler visitor = new CRSCompiler();
            visitor.visit(tree);

            String labXml = visitor.renderLab();
            try (PrintWriter pw = new PrintWriter("scene-lab.xml")) {
                pw.print(labXml);
            }

            String grdXml = visitor.renderGrid();
            try (PrintWriter pw = new PrintWriter("scene-grd.xml")) {
                pw.print(grdXml);
            }

            System.out.println("Ficheiros XML gerados: scene-lab.xml, scene-grd.xml");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}