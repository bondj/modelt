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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.honeybee.coderally.Modelquattro.Schumacher;
import org.jbox2d.common.Vec2;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.coderally.api.AbstractCarListener;
import com.ibm.coderally.entity.cars.Car;
import com.ibm.coderally.geo.CheckPoint;
import com.ibm.coderally.geo.Point;
import com.ibm.coderally.track.Track;
import com.ibm.coderally.track.Tracks;
import com.ibm.coderally.track.Tracks.TrackData;

public class Modelt extends AbstractCarListener {

	long targetSpeed = Long.MAX_VALUE;

	boolean gogogogo = false;

	Double maxTurns = Double.MIN_VALUE;
	Double maxRatio = Double.MIN_VALUE;

	@Override
	public void onTimeStep() {
		if (!gogogogo)
			return;

		Point checkpoint = schewy.getMidPoint(getCar().getCheckpoint());

		double heading = getCar().calculateHeading(checkpoint);
		double rotation;
		if (heading < 1) {
			rotation = -90;
		} else if (heading > 1) {
			rotation = 90;
		} else {
			rotation = 0;
		}

		Point position = getCar().getPosition();
		double radians = getCar().getRotation().getRadians() - Math.PI / 2 + Math.toRadians(rotation);
		Point target = new Point(position.getX() + Math.cos(radians) * 500, position.getY() + Math.sin(radians) * 500);
		getCar().setTarget(target);

		slowForTightCorner(getCar());

	}

	private void slowForTightCorner(Car mCar) {

		double turn = 0, distance = 0;
		CheckPoint cp = mCar.getCheckpoint();

		Point target = schewy.getMidPoint(cp);

		turn = Math.abs(mCar.calculateHeading(target));
		distance = mCar.getPosition().getDistance(target);
		double ratio = 0;

		double degreesPerSecond = mCar.calculateMaximumTurning(mCar.getAccelerationPercent());
		double maxAngularVelocity = Double.MIN_VALUE;
		for (int i = 0; i < 15; i++) {
			CheckPoint nextCp = getTrack().getNextCheckpoint(cp);
			double angle = Math.toDegrees(schewy.getAngle(cp));
			Point point = schewy.getMidPoint(cp);
			Point nextPoint = schewy.getMidPoint(nextCp);
			Vec2 distance2 = new Vec2((float) nextPoint.getX() - (float) point.getX(), (float) nextPoint.getY()
					- (float) point.getY());
			distance = distance + (double) distance2.length(); 

			turn = turn + Math.abs(angle);
			double seconds = distance / (mCar.getVelocity().length());

			double tempRatio = turn / (seconds * degreesPerSecond);
			if (tempRatio > ratio)
				ratio = tempRatio;

			double seconds2 = distance2.length() / mCar.getVelocity().length();
			double angularVelocity = angle / seconds2;

			if (angularVelocity > maxAngularVelocity)
				maxAngularVelocity = angularVelocity;
			cp = nextCp;
			if (seconds > 1)
				break;

		}
		System.out.println("Velocity:" + getCar().getVelocity().length());
		System.out.println("MaxAv:" + maxAngularVelocity);

		System.out.println("Ratio:" + ratio + "," + maxRatio);

		if (ratio > maxRatio)
			maxRatio = ratio;

		if (ratio > 15) {
			mCar.setBrakePercent(100);
			mCar.setAccelerationPercent(0);
		} else if (ratio > 12) {
			mCar.setBrakePercent(50);
			mCar.setAccelerationPercent(0);
		} else if (ratio > 12) {
			mCar.setBrakePercent(0);
			mCar.setAccelerationPercent(0);
		} else {
			mCar.setBrakePercent(0);
			mCar.setAccelerationPercent(100);
		}

		if (maxAngularVelocity > 360) {
			System.out.println("MAXAV BRAKING!");
			mCar.setBrakePercent(100);
			mCar.setAccelerationPercent(0);
		}

	}

	Schumacher schewy = new Schumacher();

	@Override
	public void init(Car car, Track track) {
		super.init(car, track);
		schewy.buildMap(track.getCheckpoints());
	}

	@Override
	public void onRaceStart() {
		// TODO Auto-generated method stub
		super.onRaceStart();
		getCar().setAccelerationPercent(100);
		getCar().setTarget(getCar().getCheckpoint().getCenter());
	}

	@Override
	public void onCheckpointUpdated(CheckPoint oldCheckpoint) {
		gogogogo = true;
		// getCar().setTarget(schewy.getMidPoint(getCar().getCheckpoint()));
		// double angle =
		// Math.toDegrees(schewy.getAngle(getCar().getCheckpoint()));
		// System.out.println(this + " " + getCar().getAccelerationPercent() +
		// " " + getCar().getVelocity());
		// setAcceleration(5);

	}

	public static class Schumacher {

		// public static class TrackData {
		// @JsonProperty("scale")
		// public float scale;
		// @JsonProperty("boundary_offsets")
		// public TrackBoundaries offsets;
		// @JsonProperty("max_cars")
		// public int maxCars;
		// @JsonProperty("obstacle_types")
		// public String[] obstacleTypes;
		// @JsonProperty("starting_line")
		// public CheckPoint startingLine;
		// @JsonProperty("pole_positions")
		// public PolePosition[] polePositions;
		// @JsonProperty("checkpoints")
		// public CheckPoint[] checkpoints;
		// @JsonProperty("obstacles")
		// public Rectangle[] obstacles;
		// }

		java.util.Map<CheckPoint, Double> offsets = new HashMap<CheckPoint, Double>();
		java.util.Map<CheckPoint, Double> angles = new HashMap<CheckPoint, Double>();

		public void buildMap(List<CheckPoint> origCheckpoints) {
			buildMap(origCheckpoints.toArray(new CheckPoint[0]));
		}

		double minx = Double.MAX_VALUE, maxy = Double.MIN_VALUE, miny = Double.MAX_VALUE, maxx = Double.MIN_VALUE;

		public void buildMap(CheckPoint[] origCheckpoints) {
			for (CheckPoint checkPoint : origCheckpoints) {
				offsets.put(checkPoint, Math.random());
			}
			CheckPoint[] checkpoints = new CheckPoint[origCheckpoints.length + 3];
			System.arraycopy(origCheckpoints, 0, checkpoints, 0, origCheckpoints.length);
			checkpoints[checkpoints.length - 1] = checkpoints[2];
			checkpoints[checkpoints.length - 2] = checkpoints[1];
			checkpoints[checkpoints.length - 3] = checkpoints[0];
			System.out.println(getLength(checkpoints));

			for (CheckPoint checkPoint : checkpoints) {

				for (int i = 0; i < 2; i++) {
					Point p;
					if (i == 0) {
						p = checkPoint.getStart();
					} else {
						p = checkPoint.getEnd();
					}
					if (p.getX() > maxx)
						maxx = p.getX();

					if (p.getY() > maxy)
						maxy = p.getY();

					if (p.getX() < minx)
						minx = p.getX();

					if (p.getY() < miny)
						miny = p.getY();
				}
			}

			minx = minx + 20;
			miny = miny + 20;
			maxx = maxx - 20;
			maxy = maxy - 20;

			System.out.println(minx + "," + miny + "-" + maxx + "," + maxy);

			double l1 = getLength(checkpoints);
			double delta = 0.5f;
			for (int j = 0; j < 500; j++) {
				for (int i = 0; i < checkpoints.length - 2; i++) {
					optimize2(checkpoints, i, delta);
				}
				double l2 = getLength(checkpoints);
				if (l2 == l1)
					delta = delta / 10;
				// System.out.println(getLength(checkpoints)+" "+delta);
				l1 = l2;
			}

			for (int i = 0; i < checkpoints.length - 2; i++) {
				setAngle(checkpoints[i + 1], (findAngle(checkpoints[i], checkpoints[i + 1], checkpoints[i + 2])));
			}
			System.err.println("Schewy is lock & loaded");
		}

		public void setAngle(CheckPoint key, Double value) {
			angles.put(key, value);
		}

		public void setOffset(CheckPoint key, Double value) {
			offsets.put(key, value);
		}

		public double getAngle(CheckPoint key) {
			return angles.get(key);
		}

		public double getOffset(CheckPoint key) {
			return offsets.get(key);
		}

		private void optimize2(CheckPoint[] checkpoints, int i, double alpha) {

			double alphaPlus = getOffset(checkpoints[i + 1]) + alpha;
			double alphaMinus = getOffset(checkpoints[i + 1]) - alpha;

			boolean constrained = true;
			double margin = 0.05d;
			if (constrained) {
				if (alphaMinus < margin)
					alphaMinus = margin;

				if (alphaPlus > (1 - margin))
					alphaPlus = (1 - margin);
			}

			double newOffset = getOffset(checkpoints[i + 1]);
			double length = sumSquareOfAllAngles(checkpoints);

			setOffset(checkpoints[i + 1], alphaPlus);
			double length1 = sumSquareOfAllAngles(checkpoints);

			Point testPoint = getMidPoint(checkpoints[i + 1]);

			if (length1 < length
					&& (testPoint.getX() < maxx && testPoint.getX() > minx && testPoint.getY() > miny && testPoint
							.getY() < maxy))
				newOffset = alphaPlus;

			setOffset(checkpoints[i + 1], alphaMinus);
			testPoint = getMidPoint(checkpoints[i + 1]);
			double length2 = sumSquareOfAllAngles(checkpoints);
			if (length2 < length
					&& (testPoint.getX() < maxx && testPoint.getX() > minx && testPoint.getY() > miny && testPoint
							.getY() < maxy))
				newOffset = alphaMinus;

			setOffset(checkpoints[i + 1], newOffset);

			// System.out.println(length + " " + length1 + " " + length2);
		}

		private double sumSquareOfAllAngles(CheckPoint[] checkpoints) {
			double result = 0;

			for (int i = 0; i < checkpoints.length - 2; i++) {
				Point p0 = getMidPoint(checkpoints[i]);
				Point p1 = getMidPoint(checkpoints[i + 1]);
				Point p2 = getMidPoint(checkpoints[i + 2]);
				double ang = findAngle(p0, p1, p2);
				result = result + (ang * ang * ang);
			}

			return result;
		}

		public double findAngle(CheckPoint p0, CheckPoint p1, CheckPoint p2) {
			return findAngle(getMidPoint(p0), getMidPoint(p1), getMidPoint(p2));
		}

		public double findAngle(Point p0, Point p1, Point p2) {
			return findAngle(p1.getX() - p0.getX(), p1.getY() - p0.getY(), p2.getX() - p1.getX(), p2.getY() - p1.getY());
		}

		private double findAngle(double ax, double ay, double bx, double by) {
			double result;

			double dotProduct = ax * bx + ay * by;

			double magA = Math.sqrt(ax * ax + ay * ay);
			double magB = Math.sqrt(bx * bx + by * by);

			double cos = dotProduct / (magA * magB);
			result = Math.acos(cos);

			return result;
		}
		
		private double getLength(CheckPoint[] checkpoints) {
			double result = 0;

			for (int i = 0; i < checkpoints.length - 1; i++) {
				CheckPoint start = checkpoints[i];
				CheckPoint end = checkpoints[i + 1];

				Point p1 = getMidPoint(start.getStart(), start.getEnd(), getOffset(start));
				Point p2 = getMidPoint(end.getStart(), end.getEnd(), getOffset(end));

				result = result + p1.getDistance(p2);
			}

			return result;
		}

		public Point getMidPoint(CheckPoint cp) {
			return getMidPoint(cp.getStart(), cp.getEnd(), getOffset(cp));

		}

		public Point getMidPoint(Point start, Point end, double offset) {
			Point result;

			double sx = start.getX();
			double sy = start.getY();
			double ex = end.getX();
			double ey = end.getY();

			double dx = ex - sx;
			double dy = ey - sy;

			result = new Point(sx + (dx * offset), sy + (dy * offset));

			return result;
		}
	}
	
	public static void main(String[] args) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		InputStream assets = null;
		TrackData data;

		try {
			assets = Tracks.class.getResourceAsStream("/tracks/space/track.json");
			data = mapper.readValue(assets, TrackData.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(assets);
		}
		Schumacher schewy = new Modelt.Schumacher();

		Walker walker = new Walker(data.checkpoints, schewy);
		schewy.buildMap(data.checkpoints);
		walker.showIt();

	}
}