package day22;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.rangeClosed;
import static org.assertj.core.api.Assertions.assertThat;

class DayTwentyTwoTest {

	@Test
	void partOneSample() throws Exception {
		String raw = """
				on x=-20..26,y=-36..17,z=-47..7
				on x=-20..33,y=-21..23,z=-26..28
				on x=-22..28,y=-29..23,z=-38..16
				on x=-46..7,y=-6..46,z=-50..-1
				on x=-49..1,y=-3..46,z=-24..28
				on x=2..47,y=-22..22,z=-23..27
				on x=-27..23,y=-28..26,z=-21..29
				on x=-39..5,y=-6..47,z=-3..44
				on x=-30..21,y=-8..43,z=-13..34
				on x=-22..26,y=-27..20,z=-29..19
				off x=-48..-32,y=26..41,z=-47..-37
				on x=-12..35,y=6..50,z=-50..-2
				off x=-48..-32,y=-32..-16,z=-15..-5
				on x=-18..26,y=-33..15,z=-7..46
				off x=-40..-22,y=-38..-28,z=23..41
				on x=-16..35,y=-41..10,z=-47..6
				off x=-32..-23,y=11..30,z=-14..3
				on x=-49..-5,y=-3..45,z=-29..18
				off x=18..30,y=-20..-8,z=-3..13
				on x=-41..9,y=-7..43,z=-33..15
				on x=-54112..-39298,y=-85059..-49293,z=-27449..7877
				on x=967..23432,y=45373..81175,z=27513..53682
				""";
		assertThat(cubesOnAfterSequence(List.of(raw.split("\n")))).isEqualByComparingTo(590784L);
	}

	@Test
	void partOneInput() throws Exception {
		List<String> lines = Files.readAllLines(Paths.get(getClass().getResource("input").toURI()));
		assertThat(cubesOnAfterSequence(lines)).isEqualByComparingTo(598616L);
	}

	private static long cubesOnAfterSequence(List<String> lines) {
		List<Step> list = lines.stream().map(Parser::parse).toList();
		Reactor reactor = new Reactor(Map.of());
		for (Step step : list) {
			System.out.println(step);
			reactor = reactor.apply(step);
		}
		return reactor.withinRegion().count();
	}

}

class Parser {

	private static final Pattern PATTERN = Pattern
			.compile("%s x=%s\\.\\.%s,y=%s\\.\\.%s,z=%s\\.\\.%s".formatted(
					"(?<op>on|off)",
					"(?<minX>-?[0-9]+)",
					"(?<maxX>-?[0-9]+)",
					"(?<minY>-?[0-9]+)",
					"(?<maxY>-?[0-9]+)",
					"(?<minZ>-?[0-9]+)",
					"(?<maxZ>-?[0-9]+)"));

	static Step parse(String line) {
		Matcher matcher = PATTERN.matcher(line);
		assert matcher.matches();
		boolean op = matcher.group("op").equals("on");
		var c1 = new Coordinate(
				Integer.parseInt(matcher.group("minX")),
				Integer.parseInt(matcher.group("minY")),
				Integer.parseInt(matcher.group("minZ")));
		var c2 = new Coordinate(
				Integer.parseInt(matcher.group("maxX")),
				Integer.parseInt(matcher.group("maxY")),
				Integer.parseInt(matcher.group("maxZ")));
		return new Step(op, c1, c2);
	}

}

record Reactor(Map<Coordinate, Boolean> coordinates) {

	static final Coordinate regionStart = new Coordinate(-50, -50, -50);
	static final Coordinate regionEnd = new Coordinate(50, 50, 50);

	public Reactor apply(Step step) {
		Map<Coordinate, Boolean> newCoordinates = new HashMap<>(coordinates);
		newCoordinates.putAll(step.coordinates(regionStart, regionEnd)
				.collect(toMap(identity(), c -> step.turnOn())));
		return new Reactor(newCoordinates);
	}

	public boolean withinRegion(Coordinate c) {
		return 0 <= c.compareTo(regionStart) && 0 <= regionEnd.compareTo(c);
	}

	public Stream<Coordinate> withinRegion() {
		return coordinates.entrySet().stream()
				.filter(Map.Entry::getValue)
				.map(Map.Entry::getKey)
				.filter(this::withinRegion);
	}

}

record Step(boolean turnOn, Coordinate from, Coordinate to) {

	Stream<Coordinate> coordinates(Coordinate regionStart, Coordinate regionEnd) {
		return rangeFor(Coordinate::x)
				.mapToObj(x -> rangeFor(Coordinate::y)
						.mapToObj(y -> rangeFor(Coordinate::z)
								.mapToObj(z -> new Coordinate(x, y, z))))
				.flatMap(identity())
				.flatMap(identity());
	}

	private IntStream rangeFor(Function<Coordinate, Integer> f) {
		return rangeClosed(
				Math.max(-50, Math.min(f.apply(from), f.apply(to))),
				Math.min(50, Math.max(f.apply(from), f.apply(to))));
	}

}

record Coordinate(int x, int y, int z) implements Comparable<Coordinate> {

	@Override
	public int compareTo(Coordinate that) {
		return comparing(Coordinate::x)
				.thenComparing(Coordinate::y)
				.thenComparing(Coordinate::z)
				.compare(this, that);
	}

}
