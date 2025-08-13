import org.antlr.v4.runtime.tree.*;
import org.stringtemplate.v4.*;
import java.util.*;

public class CRSCompiler extends crsBaseVisitor<Void> {
    private STGroup templates = new STGroupFile("crs_xml.stg");
    private List<ST> beacons = new ArrayList<>();
    private List<ST> targets = new ArrayList<>();
    private List<ST> walls = new ArrayList<>();
    private List<ST> positions = new ArrayList<>();
    private List<ST> labElements = new ArrayList<>();
    private String labName = "GeneratedLab";
    private String width = "28.0";
    private String height = "14.0";
    private Map<String, String> variables = new HashMap<>();

    private String resolve(String value) {
        if (variables.containsKey(value)) return variables.get(value);
        return value;
    }

    @Override
        public Void visitScene(crsParser.SceneContext ctx) {
        for (crsParser.StatementContext stmt : ctx.statement()) {
            visit(stmt);
        }
        return null;
    }

    @Override
    public Void visitRealDecl(crsParser.RealDeclContext ctx) {
        for (crsParser.AssignmentContext assign : ctx.assignmentList().assignment()) {
            String var = assign.ID().getText();
            String val = assign.decimal().getText();
            variables.put(var, val);
        }
        return null;
    }

    @Override
    public Void visitAssignment(crsParser.AssignmentContext ctx) {
        String var = ctx.ID().getText();
        String val = ctx.decimal().getText();
        variables.put(var, val);
        return null;
    }

    @Override
    public Void visitBeaconDecl(crsParser.BeaconDeclContext ctx) {
        ST st = templates.getInstanceOf("beacon");
        st.add("x", resolve(ctx.decimal(0).getText()));
        st.add("y", resolve(ctx.decimal(1).getText()));
        st.add("height", resolve(ctx.decimal(2).getText()));
        labElements.add(st);
        return null;
    }

    @Override
    public Void visitTargetDecl(crsParser.TargetDeclContext ctx) {
        ST st = templates.getInstanceOf("target");
        st.add("x", resolve(ctx.decimal(0).getText()));
        st.add("y", resolve(ctx.decimal(1).getText()));
        st.add("radius", resolve(ctx.decimal(2).getText()));
        labElements.add(st);
        return null;
    }

    @Override
    public Void visitWallDecl(crsParser.WallDeclContext ctx) {
        ST wall = templates.getInstanceOf("wall");
        wall.add("height", ctx.decimal().getText());

        List<String> cornersList = new ArrayList<>();
        double lastX = 0, lastY = 0;
        boolean first = true;

        for (crsParser.CoordinateContext coord : ctx.coordList().coordinate()) {
            double x, y;
            if (coord.INCREMENT() != null) {
                x = lastX + Double.parseDouble(resolve(coord.decimal(0).getText()));
                y = lastY + Double.parseDouble(resolve(coord.decimal(1).getText()));
            } else {
                x = Double.parseDouble(resolve(coord.decimal(0).getText()));
                y = Double.parseDouble(resolve(coord.decimal(1).getText()));
            }
            lastX = x;
            lastY = y;
            ST corner = templates.getInstanceOf("corner");
            corner.add("x", x);
            corner.add("y", y);
            cornersList.add(corner.render());
        }
        wall.add("corners", cornersList);
        labElements.add(wall);
        return null;
    }

    @Override
    public Void visitSpotDecl(crsParser.SpotDeclContext ctx) {
        ST pos = templates.getInstanceOf("position");
        pos.add("x", ctx.decimal(0).getText());
        pos.add("y", ctx.decimal(1).getText());
        pos.add("dir", ctx.decimal(2).getText());
        positions.add(pos);
        return null;
    }

    public String renderLab() {
        ST lab = templates.getInstanceOf("lab");
        lab.add("name", labName);
        lab.add("width", width);
        lab.add("height", height);
        lab.add("elements", labElements.stream().map(ST::render).toList());
        return lab.render();
    }

    public String renderGrid() {
        ST grid = templates.getInstanceOf("grid");
        grid.add("positions", positions.stream().map(ST::render).toList());
        return grid.render();
    }
}