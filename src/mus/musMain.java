import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.stringtemplate.v4.*;

public class musMain {
  public static void main(String[] args) {
    try {
      CharStream input = CharStreams.fromStream(System.in);
      musLexer lexer = new musLexer(input);
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      musParser parser = new musParser(tokens);
     ParseTree tree = parser.main();
      if (parser.getNumberOfSyntaxErrors() == 0) {
        System.out.println(tree.toStringTree(parser));
        Compiler visitor0 = new Compiler();
        ST program = visitor0.visit(tree);

        if (visitor0.getSemanticErrorCount() > 0) {
            System.err.println("Foram encontrados " + visitor0.getSemanticErrorCount() + " erros semânticos.");
            System.exit(1);
        } else {
            System.out.println("Análise semântica concluída sem erros.");
        }

	System.out.println("*************************************************");
        System.out.println(program.render());
        String filename = "Output.java";
        try {
          PrintWriter pw = new PrintWriter(new File(filename));
          pw.print(program.render());
          pw.close();
        } catch (IOException e) {
          System.err.println("ERROR: unable to write in file " + filename);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    } catch (RecognitionException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
