package pl.adamborowski.dcframework.node;

public interface Problem<Params, Result> {

    public static class DividedParams<Params> {
        public Params leftParams;
        public Params rightParams;
    }

    boolean testDivide(Params params);

    Result merge(Result left, Result right);

    Result compute(Params params);

    DividedParams<Params> divide(Params params);

}
