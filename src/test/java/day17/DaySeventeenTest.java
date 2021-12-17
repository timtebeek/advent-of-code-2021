package day17;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DaySeventeenTest {

	private static final String SAMPLE = "target area: x=20..30, y=-10..-5";
	private static final String INPUT = "target area: x=179..201, y=-109..-63";

	@Test
	void partOneSample() {
		assertThat(findTrajectoryMaxY(SAMPLE, true)).isEqualTo(45);
	}

	@Test
	void partOneInput() {
		assertThat(findTrajectoryMaxY(INPUT, true)).isEqualTo(5886);
	}

	@Test
	void partTwoSample() {
		assertThat(findTrajectoryMaxY(SAMPLE, false)).isEqualTo(112);
	}

	@Test
	void partTwoInput() {
		assertThat(findTrajectoryMaxY(INPUT, false)).isEqualTo(1806);
	}

	private static int findTrajectoryMaxY(String input, boolean partOne) {
		Target target = Target.parse(input);
		List<Integer> maxYs = new ArrayList<>();
		for (int xVelocity = 1; xVelocity <= target.maxX(); xVelocity++) {
			for (int yVelocity = target.minY(); yVelocity <= Math.abs(target.minY()); yVelocity++) {
				List<Point> trajectory = Stream.iterate(new Probe(new Point(0, 0), xVelocity, yVelocity), Probe::step)
						.map(Probe::position)
						.takeWhile(p -> !target.overshot(p))
						.toList();
				if (trajectory.stream().anyMatch(target::contains)) {
					maxYs.add(trajectory.stream().map(Point::y).max(Integer::compare).get());
				}
			}
		}
		if (partOne) {
			return maxYs.stream().max(Integer::compare).get();
		}
		return maxYs.size();
	}

}

record Probe(Point position, int xVelocity, int yVelocity) {

	Probe step() {
		return new Probe(
				new Point(
						position.x() + xVelocity,
						position.y() + yVelocity),
				0 < xVelocity ? xVelocity - 1 : 0,
				yVelocity - 1);
	}

}

record Target(int minX, int maxX, int minY, int maxY) {

	private static final Pattern PATTERN = Pattern.compile("target area: x=%s\\.\\.%s, y=%s\\.\\.%s".formatted(
			"(?<minX>[0-9]+)",
			"(?<maxX>[0-9]+)",
			"(?<minY>-[0-9]+)",
			"(?<maxY>-[0-9]+)"));

	static Target parse(String line) {
		Matcher matcher = PATTERN.matcher(line);
		if (!matcher.matches()) {
			throw new IllegalStateException("Could not match pattern");
		}
		return new Target(
				Integer.parseInt(matcher.group("minX")),
				Integer.parseInt(matcher.group("maxX")),
				Integer.parseInt(matcher.group("minY")),
				Integer.parseInt(matcher.group("maxY")));
	}

	boolean contains(Point p) {
		return minX <= p.x() && p.x() <= maxX && minY <= p.y() && p.y() <= maxY;
	}

	boolean overshot(Point p) {
		return maxX < p.x() || p.y() < minY;
	}

}

record Point(int x, int y) {
}
