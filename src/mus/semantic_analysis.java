import java.util.*;

public class semantic_analysis {
    private Map<String, Symbol> symbolTable = musParser.symbolTable;
    private int errorCount = 0;
    private static final Set<String> RESERVED = Set.of("background", "scene", "agent", "integer", "real", "angle", "string");

    public boolean declareVariable(String name, Type type) {
        if (RESERVED.contains(name)) {
            error("Não se pode usar a palavra '" + name + "' porque é reservada.");
            return false;
        }
        if (symbolTable.containsKey(name)) {
            error("Variável '" + name + "' já declarada.");
            return false;
        }
        symbolTable.put(name, new VariableSymbol(name, type));
        return true;
    }

    public boolean isDeclared(String name) {
        return symbolTable.containsKey(name);
    }

    public Type getType(String name) {
        Symbol sym = symbolTable.get(name);
        return (sym != null) ? sym.type() : null;
    }

    public boolean checkAssignmentType(String varName, Type expr) {
        if (!isDeclared(varName)) {
            error("Variável '" + varName + "' não declarada.");
            return false;
        }
        Type varType = getType(varName);
        if (expr == null) {
            error("A expressão atribuída a '" + varName + "' não tem tipo definido.");
            return false;
        }
        if (!expr.name().equals(varType.name())) {
            error("Atribuição de tipo incompatível em '" + varName + "'. Esperado: "
                + varType.name() + ", encontrado: " + expr.name());
            return false;
        }
        return true;
    }

    public boolean checkTypeCast(Type target, Type expr) {
        if (target instanceof IntegerType && expr instanceof StringType) {
            return true;
        }
        if ((target instanceof IntegerType || target instanceof RealType) &&
            (expr instanceof IntegerType || expr instanceof RealType)) {
            return true;
        }
        if (target instanceof AngleType && expr instanceof StringType) {
            error("Typecast inválido: não é possível converter string para angle.");
            return false;
        }
        if (target instanceof StringType && !(expr instanceof StringType)) {
            error("Typecast inválido: só é possível converter string para string.");
            return false;
        }
        if (target instanceof AngleType && !(expr instanceof IntegerType || expr instanceof RealType)) {
            error("Typecast inválido: não é possível converter " + expr.name() + " para angle.");
            return false;
        }
        if (target instanceof IntegerType && !(expr instanceof IntegerType || expr instanceof RealType || expr instanceof StringType)) {
            error("Typecast inválido: não é possível converter " + expr.name() + " para integer.");
            return false;
        }
        if (target instanceof RealType && !(expr instanceof IntegerType || expr instanceof RealType)) {
            error("Typecast inválido: não é possível converter " + expr.name() + " para real.");
            return false;
        }
        if (!target.name().equals(expr.name())) {
            error("Typecast inválido: não é possível converter " + expr.name() + " para " + target.name() + ".");
            return false;
        }
        return true;
    }

    public Type checkBinaryOp(Type left, Type right, String op) {
        if (left == null || right == null) {
            error("Operação '" + op + "' com operandos indefinidos.");
            return null;
        }
        if (op.matches("[+\\-*/]")) {
            if ((left.name().equals("angle") && isNumeric(right)) ||
                (right.name().equals("angle") && isNumeric(left))) {
                return new AngleType();
            }
            if ((isNumeric(left) && isNumeric(right))) {
                if (left.name().equals("real") || right.name().equals("real"))
                    return new RealType();
                return new IntegerType();
            } else if (left.name().equals("angle") && right.name().equals("angle")) {
                return new AngleType();
            } else {
                error("Operação '" + op + "' inválida para tipos: " + left.name() + " e " + right.name());
                return null;
            }
        }
        if (op.matches("==|!=|<|>|<=|>=")) {
            if (left.name().equals(right.name()) ||
                (isNumeric(left) && isNumeric(right))) {
            }
            error("Comparação inválida entre tipos: " + left.name() + " e " + right.name());
            return null;
        }
        error("Operador '" + op + "' não suportado.");
        return null;
    }

    public boolean checkVariableType(String name, Type expected) {
        if (!isDeclared(name)) {
            error("Variável '" + name + "' não declarada.");
            return false;
        }
        Type t = getType(name);
        if (!t.name().equals(expected.name())) {
            error("Tipo da variável '" + name + "' incompatível. Esperado: " + expected.name() + ", encontrado: " + t.name());
            return false;
        }
        return true;
    }

    private boolean isNumeric(Type t) {
        return t != null && (t.name().equals("integer") || t.name().equals("real"));
    }

    public void error(String msg) {
        System.err.println("Erro semântico: " + msg);
        errorCount++;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void clear() {
        symbolTable.clear();
        errorCount = 0;
    }
}