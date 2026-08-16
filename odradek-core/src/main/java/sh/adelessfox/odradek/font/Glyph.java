package sh.adelessfox.odradek.font;

import wtf.reversed.toolbox.math.Vector2;

import java.awt.geom.Path2D;
import java.util.List;

public record Glyph(
    int codePoint,
    Metrics metrics,
    Bounds bounds,
    List<Contour> contours
) {
    public Glyph {
        contours = List.copyOf(contours);
    }

    public Path2D.Float toPath() {
        var path = new Path2D.Float();
        for (Contour contour : contours()) {
            contour.evaluate(path);
        }
        return path;
    }

    public record Contour(List<Command> commands, List<Vector2> points) {
        public Contour {
            commands = List.copyOf(commands);
            points = List.copyOf(points);
        }

        public void evaluate(Path2D.Float path) {
            var first = points.getFirst();
            path.moveTo(first.x(), first.y());

            int index = 0;
            for (Command command : commands) {
                index = command.evaluate(points, index, path);
            }

            path.closePath();
        }
    }

    public sealed interface Command {
        int evaluate(List<Vector2> points, int start, Path2D.Float path);

        record LineTo(int count) implements Command {
            @Override
            public int evaluate(List<Vector2> points, int start, Path2D.Float path) {
                for (int i = 1; i <= count; i++) {
                    var point = points.get(start + i);
                    path.lineTo(point.x(), point.y());
                }

                return start + count;
            }
        }

        record CurveTo(int count) implements Command {
            @Override
            public int evaluate(List<Vector2> points, int start, Path2D.Float path) {
                float x1 = points.get(start).x();
                float y1 = points.get(start).y();

                for (int i = 1; i < count; i++) {
                    float x2 = points.get(start + i).x();
                    float y2 = points.get(start + i).y();
                    float x3 = points.get(start + i + 1).x();
                    float y3 = points.get(start + i + 1).y();

                    if (i < count - 1) {
                        x3 = (x2 + x3) / 2;
                        y3 = (y2 + y3) / 2;
                    }

                    path.curveTo(x1, y1, x2, y2, x3, y3);
                    x1 = x3;
                    y1 = y3;
                }

                return start + count;
            }
        }
    }

    public record Metrics(float advanceWidth) {
    }

    public record Bounds(float x, float y, float width, float height) {
    }
}
