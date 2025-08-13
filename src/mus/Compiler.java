import org.stringtemplate.v4.*;

@SuppressWarnings("CheckReturnValue")
public class Compiler extends musBaseVisitor<ST> {

  public semantic_analysis semantics = new semantic_analysis();
  private int varCount = 0;
  private STGroup allTemplates = new STGroupFile("java.stg");

  public int getSemanticErrorCount() {
    return semantics.getErrorCount();
  }

  @Override
  public ST visitMain(musParser.MainContext ctx) {
    ST res = allTemplates.getInstanceOf("module");
    res.add("name", "Output");
    ST decl = allTemplates.getInstanceOf("decl");
    decl.add("type", "ciberIF");
    decl.add("var", "cif");
    res.add("stat", decl.render());

    ST constr = allTemplates.getInstanceOf("constructor");
    constr.add("name", "Output");
    constr.add("body", "cif = new ciberIF();");
    res.add("stat", constr.render());

    if (ctx.block() != null) {
      for (musParser.BlockContext block : ctx.block()) {
        ST blockST = visit(block);
        if (blockST != null) {
          res.add("stat", blockST.render());
        }
      }
    }
    return res;
  }

  @Override
  public ST visitAgent(musParser.AgentContext ctx) {
    if (isOnlyScenePrintAgent(ctx)) {
      ST main = allTemplates.getInstanceOf("main");
      ST agent = allTemplates.getInstanceOf("newObj");
      agent.add("type", "agent");
      agent.add("var", "theAgent");
      agent.add("obj", "agent");
      main.add("stat", agent.render());
      ST func = allTemplates.getInstanceOf("funcCall");
      func.add("pre", "theAgent");
      func.add("func", "run");
      main.add("stat", func.render());

      ST res = allTemplates.getInstanceOf("run");
      res.add("main", main.render());
      res.add("funcs", printSceneMethod());
      res.add("stat", "System.out.print(\"Insert scene file path\");");
      res.add("stat", "String fname = new java.util.Scanner(System.in).nextLine();");
      res.add("stat", "printScene(fname);");
      return res;
    }
    ST main = allTemplates.getInstanceOf("main");
    ST agent = allTemplates.getInstanceOf("newObj");
    agent.add("type", "agent");
    agent.add("var", "theAgent");
    agent.add("obj", "agent");
    main.add("stat", agent.render());
    ST func = allTemplates.getInstanceOf("funcCall");
    func.add("pre", "theAgent");
    func.add("func", "run");
    main.add("stat", func.render());

    ST readSensor = allTemplates.getInstanceOf("func");
    readSensor.add("visibility", "public");
    readSensor.add("type", "void");
    readSensor.add("name", "readSensors");
    ST dow = allTemplates.getInstanceOf("dow");
    ST dowVar = allTemplates.getInstanceOf("funcCall");
    dow.add("func", "cif.GetStartButton() == false");
    ST dowStat = allTemplates.getInstanceOf("funcCall");
    dowStat.add("pre", "cif");
    dowStat.add("func", "ReadSensors");
    dow.add("stat", dowStat.render());
    readSensor.add("stat", dow.render());

    ST wait = allTemplates.getInstanceOf("func");
    wait.add("visibility", "public");
    wait.add("type", "void");
    wait.add("name", "wait");
    wait.add("args", "int n");
    ST forL = allTemplates.getInstanceOf("for");
    forL.add("var", "i");
    forL.add("varValue", "0");
    forL.add("comp", "n");
    ST readFunc = allTemplates.getInstanceOf("funcCall");
    readFunc.add("func", "readSensors");
    forL.add("stat", readFunc.render());
    wait.add("stat", forL.render());

    ST res = allTemplates.getInstanceOf("run");
    res.add("main", main.render());
    res.add("funcs", readSensor.render());
    res.add("funcs", wait.render());
    if (ctx.command() != null) {
      for (musParser.CommandContext command : ctx.command()) {
        res.add("stat", visit(command));
      }
    }
    return res;
  }

  @Override
  public ST visitBehavior(musParser.BehaviorContext ctx) {
    ST res = allTemplates.getInstanceOf("func");
    res.add("visibility", "private");
    res.add("type", "void");
    res.add("name", ctx.ID().getText());
    if (ctx.input() != null) {
      ST args = allTemplates.getInstanceOf("args");
      for (musParser.InputContext input : ctx.input()) {
        args.add("arg", visit(input).render());
      }
      res.add("args", args.render());
    }
    if (ctx.command() != null) {
      for (musParser.CommandContext command : ctx.command()) {
        res.add("stat", visit(command));
      }
    }
    return res;
  }

  @Override
  public ST visitType(musParser.TypeContext ctx) {
    return allTemplates.getInstanceOf("stats");
  }

  @Override
  public ST visitInput(musParser.InputContext ctx) {
    ST res = allTemplates.getInstanceOf("initFuncArg");
    String id = ctx.ID().getText();
    Type type = ctx.type().res;
    VariableSymbol s = new VariableSymbol(newVarName(), type);
    s.setVarName(id);
    musParser.symbolTable.put(s.varName(), s);
    res.add("type", ctx.type().getText());
    res.add("id", s.name());
    return res;
  }

  @Override
  public ST visitLoadCommand(musParser.LoadCommandContext ctx) {
    ST res = allTemplates.getInstanceOf("decl");
    String varName = newVarName();
    String fileExpr = ctx.String() != null ? ctx.String().getText() : ctx.ID().getText();
    res.add("type", "scene");
    res.add("var", varName);
    res.add("value", fileExpr);
    return res;
  }

  /*
  @Override
  public ST visitCommand(musParser.CommandContext ctx) {
    ST res = null;
    return visitChildren(ctx);
    // return res;
  }
  */

  @Override
  public ST visitConnect(musParser.ConnectContext ctx) {
    ST res = allTemplates.getInstanceOf("connect");
    ST stats = allTemplates.getInstanceOf("stats");
    stats.add("stat", visit(ctx.name()).render());
    res.add("name", ctx.name().varName);
    stats.add("stat", visit(ctx.host()).render());
    res.add("host", ctx.host().varName);
    if (ctx.port() != null) {
      stats.add("stat", visit(ctx.port()).render());
      res.add("port", ctx.port().varName);
    }
    res.add("stat", stats.render());
    return res;
  }

  @Override
  public ST visitName(musParser.NameContext ctx) {
    ST res = visit(ctx.expr());
    ctx.varName = ctx.expr().varName;
    return res;
  }

  @Override
  public ST visitHost(musParser.HostContext ctx) {
    ST res = visit(ctx.expr());
    ctx.varName = ctx.expr().varName;
    return res;
  }

  @Override
  public ST visitPort(musParser.PortContext ctx) {
    ST res = visit(ctx.expr());
    ctx.varName = ctx.expr().varName;
    return res;
  }

  @Override
  public ST visitAssignment(musParser.AssignmentContext ctx) {
    ST res = allTemplates.getInstanceOf("assign");
    String id = ctx.ID().getText();
    ST exprRes = visit(ctx.expr());
    semantics.checkAssignmentType(id, ctx.expr().eType);
    Symbol s = musParser.symbolTable.get(id);
    res.add("stat", exprRes.render());
    res.add("var", s != null ? s.name() : id);
    res.add("value", ctx.expr().varName);
    return res;
  }

  @Override
  public ST visitExprUnitary(musParser.ExprUnitaryContext ctx) {
    ST res = allTemplates.getInstanceOf("stats");
    res.add("stat", visit(ctx.expr()).render());
    ST decl = allTemplates.getInstanceOf("decl");
    ctx.varName = newVarName();
    decl.add("type", ctx.expr().eType.name());
    decl.add("var", ctx.varName);
    decl.add("value", ctx.op.getText() + ctx.expr().varName);
    res.add("stat", decl.render());
    return res;
  }

  @Override
  public ST visitExprRead(musParser.ExprReadContext ctx) {
    ST res = visit(ctx.read());
    ctx.varName = ctx.read().varName;
    ctx.eType = new StringType();
    return res;
  }

  @Override
  public ST visitExprString(musParser.ExprStringContext ctx) {
    ST res = allTemplates.getInstanceOf("decl");
    ctx.varName = newVarName();
    ctx.eType = new StringType();
    String value = ctx.String().getText();
    if (value.charAt(0) != '"') {
      value = "\"" + value + "\"";
    }
    res.add("type", "string");
    res.add("var", ctx.varName);
    res.add("value", value);
    return res;
  }

  @Override
  public ST visitExprParent(musParser.ExprParentContext ctx) {
    ST res = visit(ctx.expr());
    ctx.varName = ctx.expr().varName;
    ctx.eType= ctx.expr().eType;
    return res;
  }

  @Override
  public ST visitExprOp(musParser.ExprOpContext ctx) {
    ST res = allTemplates.getInstanceOf("binaryExpression");
    ctx.varName = newVarName();
    res.add("stat", visit(ctx.e1).render());
    res.add("stat", visit(ctx.e2).render());
    String op = ctx.op.getText();
    Type resultType = semantics.checkBinaryOp(ctx.e1.eType, ctx.e2.eType, op);
    ctx.eType = resultType;
    res.add("type", resultType != null ? resultType.name() : "undefined");
    res.add("var", ctx.varName);
    res.add("e1", ctx.e1.varName);
    res.add("op", op);
    res.add("e2", ctx.e2.varName);
    return res;
  }

  @Override
  public ST visitExprReal(musParser.ExprRealContext ctx) {
    ST res = allTemplates.getInstanceOf("decl");
    ctx.varName = newVarName();
    ctx.eType = new RealType();
    res.add("type", "real");
    res.add("var", ctx.varName);
    res.add("value", ctx.Real().getText());
    return res;
  }

  @Override
  public ST visitExprSense(musParser.ExprSenseContext ctx) {
    ST res = visit(ctx.sense());
    ctx.varName = ctx.sense().varName;
    ctx.eType = new AngleType();
    return res;
  }

  @Override
  public ST visitExprInteger(musParser.ExprIntegerContext ctx) {
    ST res = allTemplates.getInstanceOf("decl");
    ctx.varName = newVarName();
    ctx.eType = new IntegerType();
    res.add("type", "integer");
    res.add("var", ctx.varName);
    res.add("value", ctx.Integer().getText());
    return res;
  }

  @Override
  public ST visitExprTypeCast(musParser.ExprTypeCastContext ctx) {
    ST res = visit(ctx.typeCast());
    ctx.varName = ctx.typeCast().varName;
    String op = ctx.typeCast().op.getText();
    Type targetType;
    switch (op) {
        case "integer": targetType = new IntegerType(); break;
        case "real":    targetType = new RealType();    break;
        case "angle":   targetType = new AngleType();   break;
        case "string":  targetType = new StringType();  break;
        default:        targetType = null;
    }
    Type exprType = ctx.typeCast().expr().eType;
    semantics.checkTypeCast(targetType, exprType);
    ctx.eType = targetType;
    return res;
  }

  @Override
  public ST visitExprLoad(musParser.ExprLoadContext ctx) {
      ST res = allTemplates.getInstanceOf("decl");
      String varName = newVarName();
      musParser.LoadCommandContext load = ctx.loadCommand();
      String fileExpr = load.String() != null ? load.String().getText() : load.ID().getText();
      res.add("type", "scene");
      res.add("var", varName);
      res.add("value", fileExpr);
      return res;
  }

  @Override
  public ST visitExprID(musParser.ExprIDContext ctx) {
    ST res = allTemplates.getInstanceOf("stats");
    ST decl = allTemplates.getInstanceOf("decl");
    String id = ctx.ID().getText();
    Symbol s = musParser.symbolTable.get(id);
    ctx.varName = newVarName();
    ctx.eType = s.type();
    decl.add("type", ctx.eType.name());
    decl.add("var", ctx.varName);
    decl.add("value", s.name());
    res.add("stat", decl.render());
    return res;
  }

  @Override
  public ST visitTypeCast(musParser.TypeCastContext ctx) {
    ST res = allTemplates.getInstanceOf("cast");
    ST stats = allTemplates.getInstanceOf("stats");
    stats.add("stat", visit(ctx.expr()).render());
    res.add("stat", stats.render());
    res.add("type", ctx.op.getText());
    ctx.varName = newVarName();
    res.add("var", ctx.varName);
    res.add("value", ctx.expr().varName);
    return res;
  }

  @Override
  public ST visitRead(musParser.ReadContext ctx) {
    ST res = allTemplates.getInstanceOf("read");
    ST string = allTemplates.getInstanceOf("print");
    string.add("expr", ctx.String().getText());
    ctx.varName = newVarName();
    res.add("stat", string.render());
    res.add("type", "String");
    res.add("var", ctx.varName);
    res.add("value", "new java.util.Scanner(System.in).nextLine()");
    return res;
  }

  @Override
  public ST visitVar(musParser.VarContext ctx) {
    ST res = allTemplates.getInstanceOf("stats");
    String id = ctx.ID().getText();
    Type type = ctx.type().res;
    semantics.declareVariable(id, type);
    VariableSymbol s = new VariableSymbol(newVarName(), type);
    s.setVarName(id);
    musParser.symbolTable.put(s.varName(), s);

    ST decl = allTemplates.getInstanceOf("decl");

    if (ctx.expr() != null) {
        ST exprST = visit(ctx.expr());
        res.add("stat", exprST.render());
        if (ctx.expr() instanceof musParser.ExprTypeCastContext) {
            decl.add("type", s.type().name());
            decl.add("var", s.name());
            decl.add("value", ctx.expr().varName);
        }
        else if (ctx.expr() instanceof musParser.ExprReadContext) {
            decl.add("type", "string");
            decl.add("var", s.name());
            decl.add("value", "new java.util.Scanner(System.in).nextLine()");
        }
        else {
            decl.add("type", s.type().name());
            decl.add("var", s.name());
            decl.add("value", ctx.expr().varName);
        }
    } else {
        decl.add("type", s.type().name());
        decl.add("var", s.name());
    }

    res.add("stat", decl.render());
    return res;
  }

  @Override
  public ST visitPrint(musParser.PrintContext ctx) {
    ST res = allTemplates.getInstanceOf("print");
    String toret = "";
    boolean sceneCall = false;
    for (musParser.ExprContext expr : ctx.expr()) {
        if (expr instanceof musParser.ExprIDContext) {
            Symbol s = musParser.symbolTable.get(((musParser.ExprIDContext)expr).ID().getText());
            if (s != null && s.type().name().equals("scene")) {
                res.add("sceneCall", "printScene(" + s.name() + ");");
                sceneCall = true;
                continue;
            }
        }
        ST string = allTemplates.getInstanceOf("decl");
        String varName = newVarName();
        string.add("type", "string");
        string.add("var", varName);
        String value = expr.getText();
        if (value.charAt(0) != '"') {
            value = "\"" + value + "\"";
        }
        string.add("value", value);
        res.add("stat", string.render());
        if (toret.equals("")) {
            toret = varName;
        } else {
            toret = toret + "+" + varName;
        }
    }
    if (!sceneCall) {
        res.add("expr", toret);
    }
    return res;
  }

  @Override
  public ST visitWait_command(musParser.Wait_commandContext ctx) {
    ST res = allTemplates.getInstanceOf("wait");
    res.add("stat", visit(ctx.expr()).render());
    res.add("value", ctx.expr().varName);
    return res;
  }

  @Override
  public ST visitStop(musParser.StopContext ctx) {
    ST res = allTemplates.getInstanceOf("drive");
    res.add("e1", "0");
    res.add("e2", "0");
    return res;
  }

  @Override
  public ST visitSpeed(musParser.SpeedContext ctx) {
    ST res = allTemplates.getInstanceOf("drive");
    ST stats = allTemplates.getInstanceOf("stats");
    stats.add("stat", visit(ctx.expr()).render());
    res.add("stat", stats.render());
    res.add("e1", ctx.expr().varName);
    res.add("e2", ctx.expr().varName);
    return res;
  }

  @Override
  public ST visitTwistAction(musParser.TwistActionContext ctx) {
    return allTemplates.getInstanceOf("stats");
  }

  @Override
  public ST visitRotate(musParser.RotateContext ctx) {
    ST res = allTemplates.getInstanceOf("drive");
    ST stats = allTemplates.getInstanceOf("stats");
    stats.add("stat", visit(ctx.expr()).render());
    res.add("stat", stats.render());
    if (ctx.direction != null) {
      if (ctx.direction.getText().equals("right")) {
	res.add("stat", ctx.expr().varName + " = -" + ctx.expr().varName + ";");
      }
    }
    res.add("e1", "-" + ctx.expr().varName);
    res.add("e2", ctx.expr().varName);
    return res;
  }

  @Override
  public ST visitRun(musParser.RunContext ctx) {
    ST res = allTemplates.getInstanceOf("funcCall");
    res.add("func", ctx.ID().getText());
    for (musParser.ArgsContext args : ctx.args()) {
      res.add("stat", visit(args).render());
      res.add("arg", args.varName);
    }
    return res;
  }

  @Override
  public ST visitArgs(musParser.ArgsContext ctx) {
    ST res = visit(ctx.expr());
    ctx.varName = ctx.expr().varName;
    return res;
  }

  @Override
  public ST visitTurnAction(musParser.TurnActionContext ctx) {
    ST res = allTemplates.getInstanceOf("wait");
    if (ctx.ledName().getText().equals("finish")) {
      res.add("stat", "cif.Finish();");
      res.add("value", "1");
    }
    return res;
  }

  @Override
  public ST visitLedName(musParser.LedNameContext ctx) {
    return allTemplates.getInstanceOf("stats");
  }

  @Override
  public ST visitSense(musParser.SenseContext ctx) {
      ST res = allTemplates.getInstanceOf("decl");
      ctx.varName = newVarName();
      res.add("type", "angle");
      res.add("var", ctx.varName);
      if (ctx.op.getText().equals("compass")) {
          res.add("value", "cif.GetCompassSensor() * Math.PI / 180.0");
      } else {
          res.add("value", "cif.GetBeaconSensor(" + ctx.id.getText() + ").beaconDir * Math.PI / 180.0");
      }
      return res;
  }

  private String newVarName() {
    varCount++;
    return "v" + varCount;
  }

  private String printSceneMethod() {
    return """
    public void printScene(String filename) {
        try {
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(filename));
            String line;
            System.out.println("The scene:");
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();
        } catch (java.io.IOException e) {
            System.err.println("Error reading scene file: " + e.getMessage());
        }
    }
    """;
  }

  private boolean isOnlyScenePrintAgent(musParser.AgentContext ctx) {
    if (ctx.command() == null || ctx.command().size() != 4) return false;
    boolean foundRead = false, foundLoad = false, foundPrintStr = false, foundPrintScene = false;
    for (musParser.CommandContext cmd : ctx.command()) {
      if (cmd.var() != null && cmd.var().type().getText().equals("string") && cmd.var().expr() != null && cmd.var().expr().getText().startsWith("read")) {
        foundRead = true;
      } else if (cmd.var() != null && cmd.var().type().getText().equals("scene") && cmd.var().expr() != null && cmd.var().expr().getText().startsWith("load")) {
        foundLoad = true;
      } else if (cmd.print() != null && cmd.print().expr(0).getText().startsWith("\"")) { // Linha 597 alterada
        foundPrintStr = true;
      } else if (cmd.print() != null && cmd.print().expr(0).getText().equals("theScene")) {
        foundPrintScene = true;
      }
    }
    return foundRead && foundLoad && foundPrintStr && foundPrintScene;
  }
}

