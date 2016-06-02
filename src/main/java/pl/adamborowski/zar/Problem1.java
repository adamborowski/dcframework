package pl.adamborowski.zar;

import pl.adamborowski.dcframework.Problem;

import java.io.Serializable;

class Problem1 implements Problem<Problem1.Params, Double> {
    public Double compute(Params params) {
        return compute(params.a, params.b);
    }

    public boolean testDivide(Params params) {
        double middle = params.middle();
        double big = compute(params);
        double small = compute(params.a, middle) + compute(middle, params.b);
        double range = Math.abs(params.a - params.b);
        double error = Math.abs(big - small);
        return range < 0.0000001 || error > 0.0000001;
//        return range > 4;
    }

    private double compute(double a, double b) {
        double delta = b - a;
        double fa = f(a);
        double fb = f(b);
        return (fa + fb) / 2 * delta;
    }

    public Double merge(Double left, Double right) {
        return left + right;
    }

    private double f(double x) {
        return Math.sin(x + 2) * ((Math.cos(3 * x - 2))) / 0.1 * x;
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
