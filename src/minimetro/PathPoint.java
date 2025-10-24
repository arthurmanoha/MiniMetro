/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package minimetro;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import static minimetro.Cell.cellSize;

/**
 *
 * @author arthu
 */
public class PathPoint extends Point {

    public PathPoint predecessor;
    public boolean isInFinalPath;
    public double gScore, hScore;
    public boolean isStraightLine;

    public PathPoint(int x, int y, PathPoint other) {
        super(x, y);
        this.predecessor = other;
        gScore = 1000;
        hScore = 1000;
        isStraightLine = false;
    }

    public PathPoint() {
        this(0, 0, null);
    }

    public void paint(Graphics g, double x0, double y0, double zoom, Color c, int nbRows) {

        final double appSize = zoom * cellSize;
        double xApp = cellSize * (this.x - 0.5) * zoom + x0;
        double yApp = g.getClipBounds().height - (cellSize * (nbRows - this.y - 0.5) * zoom + y0);

        int margin = 0;

        // Center color is specified as a parameter
        if (this.isInFinalPath) {
            if (isStraightLine) {
                g.setColor(Color.orange);
            } else {
                g.setColor(Color.red);
            }
            g.fillRect((int) xApp + margin, (int) yApp + margin, (int) appSize - 2 * margin, (int) appSize - 2 * margin);
        } else {
            g.setColor(c);
            g.drawRect((int) xApp + margin, (int) yApp + margin, (int) appSize - 2 * margin, (int) appSize - 2 * margin);
        }
    }

    public void computeHeuristic(PathPoint end) {
        double dx = end.x - this.x;
        double dy = end.y - this.y;
        hScore = 0.5 * Math.sqrt(dx * dx + dy * dy);
    }

    protected double getF() {
        return this.gScore + this.hScore;
    }

    @Override
    public String toString() {
        return super.toString() + ", pred " + predecessor;
    }

    /**
     * Detects a turn
     * This method does NOT update the turne status of this path point.
     *
     * @param prev
     * @param next
     * @return false when the current node is aligned with prev and next
     * (i.e. not a turn), true otherwise.
     */
    public boolean computeIsStraightLine(PathPoint prev, PathPoint next) {
        boolean result;
        try {
            // A straight line is in the middle between its two neighbors,
            // A turn is not.
            result = (2 * this.x == (prev.x + next.x)) && (2 * this.y == (prev.y + next.y));

        } catch (NullPointerException e) {
            // If any of the previous and next node is undefined, result is not a turn.
            result = true;
        }
        return result;
    }
}
