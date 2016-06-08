package pl.adamborowski.zar;

import pl.adamborowski.dcframework.Problem;

import java.io.Serializable;

class DummyProblem implements Problem<DummyProblem.Params, Double> {
    public Double compute(Params params) {
        return compute(params.a, params.b);
    }

    public boolean testDivide(Params params) {
        return params.b - params.a > 1;
    }

    private double compute(double a, double b) {
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            Throwables.propagate(e);
//        }
        return b - a;
    }

    public Double merge(Double left, Double right) {
        return left + right;
    }


    public DividedParams<Params> divide(Params params) {
        DividedParams<Params> d = new DividedParams<Params>();
        d.leftParams = Params.of(params.a, params.middle());
        d.rightParams = Params.of(params.middle(), params.b);
        return d;
    }

    static class Params implements Serializable {
        public double a;
        public double b;

        public Params(double a, double b) {
            this.a = a;
            this.b = b;
        }

        static Params of(double a, double b) {
            return new Params(a, b);
        }

        double middle() {
            return (a + b) / 2;
        }

        @Override
        public String toString() {
            return "[" + a + ", " + b + ']';
        }
    }
}
