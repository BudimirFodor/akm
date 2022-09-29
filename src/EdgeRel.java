public class EdgeRel {

    private boolean relation;
    private Vertex fromVertex;
    private Vertex toVertex;

    public EdgeRel(boolean relation, Vertex fromVertex, Vertex toVertex) {
        this.relation = relation;
        this.fromVertex = fromVertex;
        this.toVertex = toVertex;
    }

    public boolean isRelation() {
        return relation;
    }

    public void setRelation(boolean relation) {
        this.relation = relation;
    }

    public Vertex getFromVertex() {
        return fromVertex;
    }

    public void setFromVertex(Vertex fromVertex) {
        this.fromVertex = fromVertex;
    }

    public Vertex getToVertex() {
        return toVertex;
    }

    public void setToVertex(Vertex toVertex) {
        this.toVertex = toVertex;
    }

    @Override
    public String toString() {
        return fromVertex.getValue() + "\t" + toVertex.getValue();
    }
}
