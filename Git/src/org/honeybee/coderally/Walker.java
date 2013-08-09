/*
 * Copyright 2013 Jonathan Bond
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.honeybee.coderally;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.UIManager;

import com.ibm.coderally.geo.CheckPoint;
import com.ibm.coderally.geo.Point;

public class Walker extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private CheckPoint[] checkpoints;
	private Modelt.Schumacher schewy;
	BufferedImage bitmap;

	public Walker(CheckPoint[] a, Modelt.Schumacher schewy) {
		this.schewy = schewy;
		checkpoints = a;

	}

	public void showIt() {
		int width = 1024;
		int height = 768;
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((int) (screen.getWidth() - width) / 2, (int) (screen.getHeight() - height) / 2);

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		this.setResizable(false);
		TrackView view = new TrackView();
		view.setPreferredSize(new Dimension(width, height));
		this.getContentPane().add(view);

		setVisible(true);
		pack();
	}

	public class TrackView extends JComponent {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void paint(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;

			g2d.setColor(Color.WHITE);
			g2d.fillRect(0, 0, 1024, 768);
			g2d.setColor(Color.RED);			

			for (int i = 0; i < checkpoints.length; i++) {
				CheckPoint p1 = checkpoints[i];
				g2d.drawLine(p1.getStart().getIntX(), p1.getStart().getIntY(), p1.getEnd().getIntX(), p1.getEnd()
						.getIntY());
			}

			for (int i = 0; i < checkpoints.length - 1; i++) {
				CheckPoint cp1 = checkpoints[i];
				CheckPoint cp2 = checkpoints[i + 1];
				Point p1 = schewy.getMidPoint(cp1.getStart(), cp1.getEnd(), schewy.getOffset(cp1));
				Point p2 = schewy.getMidPoint(cp2.getStart(), cp2.getEnd(), schewy.getOffset(cp2));

				g2d.drawLine(p1.getIntX(), p1.getIntY(), p2.getIntX(), p2.getIntY());
				// g2d.drawString(Math.toDegrees(schewy.getAngle(cp2))+"",
				// p2.getIntX(), p2.getIntY());
				/*g2d.setColor(Color.BLUE);
				List<Point> points = schewy.bezierCurve(p1.getX(), p1.getY(), Math.random()*Math.PI*2, 100, p2.getX(), p2.getY(), Math.random()*Math.PI*2, 100, 10);
				for (int j = 0; j < points.size() - 1; j++) {
					Point bp1 = points.get(j);
					Point bp2 = points.get(j + 1);
					g2d.drawLine(bp1.getIntX(), bp1.getIntY(), bp2.getIntX(), bp2.getIntY());
				}*/
			}

			{
				CheckPoint cp1 = checkpoints[checkpoints.length - 1];
				CheckPoint cp2 = checkpoints[0];
				Point p1 = schewy.getMidPoint(cp1.getStart(), cp1.getEnd(), schewy.getOffset(cp1));
				Point p2 = schewy.getMidPoint(cp2.getStart(), cp2.getEnd(), schewy.getOffset(cp2));

				g2d.setColor(Color.RED);
				g2d.drawLine(p1.getIntX(), p1.getIntY(), p2.getIntX(), p2.getIntY());
				// g2d.drawString(Math.toDegrees(schewy.getOffset(cp2))+"",
				// p2.getIntX(), p2.getIntY());
				/*g2d.setColor(Color.BLUE);
				List<Point> points = schewy.bezierCurve(p1.getX(), p1.getY(), 0, 100, p2.getX(), p2.getY(), 0, 100, 10);
				for (int j = 0; j < points.size() - 1; j++) {
					Point bp1 = points.get(j);
					Point bp2 = points.get(j + 1);
					g2d.drawLine(bp1.getIntX(), bp1.getIntY(), bp2.getIntX(), bp2.getIntY());
				}*/

			}

		}
	}

}
