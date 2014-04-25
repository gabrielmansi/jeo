/* Copyright 2014 The jeo project. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jeo.map;

import org.jeo.filter.Expression;
import org.jeo.filter.Function;
import org.jeo.filter.Literal;
import org.jeo.filter.Mixed;
import org.jeo.util.Convert;
import org.jeo.util.Interpolate;
import org.jeo.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/**
 * Maps numeric values to colors.
 * <p>
 * A colorizer is composed of levels known as "stops" that define a lookup
 * table composed of ranges of numeric values. Each range maps to a color
 * value with an optional "mode" that controls how the mapping occurs.
 * </p>
 * <p>
 * Note: Currently only {@link Mode#DISCRETE} is supported. TODO: fix this.
 * </p>
 */
public class Colorizer {

    /**
     * Stop matching mode.
     *
     * @see {@linkplain https://github.com/mapnik/mapnik/wiki/RasterColorizer#modes}
     */
    public static enum Mode {
        /**
         * Causes all input values from the stops value, up until the next stops value.
         */
        DISCRETE,
        /**
         * Causes all input values from the stops value, up until the next stops value to be translated to a color
         * which is linearly interpolated between the two stops colors
         */
        LINEAR,

        /**
         * Exact causes an input value which matches the stops value to be translated to the stops color.
         */
        EXACT;
    }

    /**
     * Default mode.
     */
    Mode mode = Mode.LINEAR;

    /**
     * Default color.
     */
    RGB color = RGB.black;

    /**
     * Stops.
     */
    List<Stop> stops = new ArrayList<Stop>();

    /**
     * The default mode for the colorizer.
     */
    public Mode mode() {
        return mode;
    }

    /**
     * The default color for the colorizer.
     */
    public RGB color() {
        return color;
    }

    /**
     * List of stops in the colorizer.
     */
    public List<Stop> stops() {
        return stops;
    }

    /**
     * Maps a value to a color based on the stops of the colorizer.
     *
     * @param value The value to map.
     *
     * @return The mapped color.
     */
    public RGB map(Double value) {
        if (value == null || value.isNaN()) {
            return color;
        }

        Stop last = null;
        for (Stop stop : stops) {
            if (stop.value >= value) {
                break;
            }
            last = stop;
        }

        return last != null ? last.color : color;
    }

    /**
     * The stop class.
     */
    public static class Stop {
        public final Double value;
        public final RGB color;
        public final Mode mode;

        public Stop(double value, RGB color, Mode mode) {
            this.value = value;
            this.color = color;
            this.mode = mode;
        }
    }

    /**
     * Encodes a colorizer into a style Rule.
     */
    public static Rule encode(Colorizer c, Rule rule) {
        rule.put("raster-colorizer-default-mode", c.mode);
        rule.put("raster-colorizer-default-color", c.color);

        Mixed mixed = new Mixed();
        for (Colorizer.Stop stop : c.stops()) {
            Function f = new Function("stop") {
                @Override
                public Object evaluate(Object obj) {
                    return null;
                }
            };
            f.getArgs().add(new Literal(stop.value));
            f.getArgs().add(new Literal(stop.color));
            mixed.getExpressions().add(f);
        }

        rule.put("raster-colorizer-stops", mixed);
        return rule;
    }

    /**
     * Decodes a colorizer from a style Rule.
     */
    public static Colorizer decode(Rule rule) {
        Colorizer.Builder cb = Colorizer.build();

        cb.mode(Colorizer.Mode.valueOf(
            rule.string(null, "raster-colorizer-default-mode", "linear").toUpperCase()));
        cb.color(rule.color(null, "raster-colorizer-default-color", RGB.black));

        if (rule.has("raster-colorizer-stops")) {
            //TODO: make this routine more robust
            Mixed m = (Mixed) rule.get("raster-colorizer-stops");
            for (Expression expr : m.getExpressions()) {
                Function stop = (Function) expr;
                List<Expression> args = stop.getArgs();
                Number value =  Convert.toNumber(args.get(0).evaluate(null)).get();
                RGB color = Convert.toColor(args.get(1).evaluate(null)).get();

                cb.stop(value.doubleValue(), color);
            }
        }

        return cb.colorizer();
    }

    /**
     * Creates a new Colorizer builder.
     */
    public static Builder build() {
        return new Builder();
    }

    /**
     * Builder class for colorizer.
     */
    public static class Builder {

        Colorizer colorizer = new Colorizer();

        TreeSet<Stop> stops = new TreeSet<Stop>(new Comparator<Stop>() {
            @Override
            public int compare(Stop stop1, Stop stop2) {
            return stop1.value.compareTo(stop2.value);
            }
        });

        /**
         * Sets the default color for the colorizer.
         */
        public Builder color(RGB color) {
            colorizer.color = color;
            return this;
        }

        /**
         * Sets the default mode for the colorizer.
         */
        public Builder mode(Mode mode) {
            colorizer.mode = mode;
            return this;
        }

        /**
         * Adds a new stop to the colorizer using the default mode.
         */
        public Builder stop(double value, RGB color) {
            return stop(value, color, null);
        }

        /**
         * Adds a new stop to the colorizer using the specified mode.
         */
        public Builder stop(double value, RGB color, Mode mode) {
            stops.add(new Stop(value, color, mode));
            return this;
        }

        /**
         * Creates a number of stop values based on linear interpolation between two value/color pairs.
         *
         * @param low The low color/value pair.
         * @param high The high color/value pair.
         * @param n The number of interpolation buckets, will product n+1 values.
         */
        public Builder interpolate(Pair<Double,RGB> low, Pair<Double,RGB> high, int n) {
            List<Double> values = Interpolate.linear(low.first(), high.first(), n);
            List<RGB> colors = low.second().interpolate(high.second(), n, Interpolate.Method.LINEAR);

            for (int i = 0; i < values.size(); i++) {
                stop(values.get(i), colors.get(i));
            }

            return this;
        }

        /**
         * Returns the build colorizer.
         */
        public Colorizer colorizer() {
            colorizer.stops.clear();
            colorizer.stops.addAll(new ArrayList<Stop>(stops));
            return colorizer;
        }
    }
}