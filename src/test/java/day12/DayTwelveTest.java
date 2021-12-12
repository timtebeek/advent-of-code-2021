package day12;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static org.assertj.core.api.Assertions.assertThat;

class DayTwelveTest {

	private static final String SAMPLE_ONE = """
			start-A
			start-b
			A-c
			A-b
			b-d
			A-end
			b-end
			""";

	@Test
	void partOneSampleOne() throws Exception {
		assertThat(distinctPathsThatVisitSmallCavesAtMostOnce(SAMPLE_ONE, false)).isEqualTo(10);
	}

	@Test
	void partTwoSampleOne() throws Exception {
		assertThat(distinctPathsThatVisitSmallCavesAtMostOnce(SAMPLE_ONE, true)).isEqualTo(36);
	}

	private static final String SAMPLE_TWO = """
			dc-end
			HN-start
			start-kj
			dc-start
			dc-HN
			LN-dc
			HN-end
			kj-sa
			kj-HN
			kj-dc
			""";

	@Test
	void partOneSampleTwo() throws Exception {
		assertThat(distinctPathsThatVisitSmallCavesAtMostOnce(SAMPLE_TWO, false)).isEqualTo(19);
	}

	@Test
	void partTwoSampleTwo() throws Exception {
		assertThat(distinctPathsThatVisitSmallCavesAtMostOnce(SAMPLE_TWO, true)).isEqualTo(103);
	}

	private static final String SAMPLE_THREE = """
			fs-end
			he-DX
			fs-he
			start-DX
			pj-DX
			end-zg
			zg-sl
			zg-pj
			pj-he
			RW-he
			fs-DX
			pj-RW
			zg-RW
			start-pj
			he-WI
			zg-he
			pj-fs
			start-RW
			""";

	@Test
	void partOneSampleThree() throws Exception {
		assertThat(distinctPathsThatVisitSmallCavesAtMostOnce(SAMPLE_THREE, false)).isEqualTo(226);
	}

	@Test
	void partTwoSampleThree() throws Exception {
		assertThat(distinctPathsThatVisitSmallCavesAtMostOnce(SAMPLE_THREE, true)).isEqualTo(3509);
	}

	private static final String INPUT = """
			KF-sr
			OO-vy
			start-FP
			FP-end
			vy-mi
			vy-KF
			vy-na
			start-sr
			FP-lh
			sr-FP
			na-FP
			end-KF
			na-mi
			lh-KF
			end-lh
			na-start
			wp-KF
			mi-KF
			vy-sr
			vy-lh
			sr-mi
			""";

	@Test
	void partOneInput() throws Exception {
		assertThat(distinctPathsThatVisitSmallCavesAtMostOnce(INPUT, false)).isEqualTo(4885);
	}

	@Test
	void partTwoInput() throws Exception {
		assertThat(distinctPathsThatVisitSmallCavesAtMostOnce(INPUT, true)).isEqualTo(117095);
	}

	private static int distinctPathsThatVisitSmallCavesAtMostOnce(String input, boolean permitSingleSmallCaveTwice) {
		CaveSystem system = CaveSystem.parse(input);
		return system.pathsFromCaveToEnd(system.start(), List.of(), permitSingleSmallCaveTwice).size();
	}

}

record CaveSystem(Cave start, Cave end, Map<String, Cave> caves) {

	static CaveSystem parse(String input) {
		Map<String, Cave> caves = new HashMap<>();
		String[] lines = input.split("\n");
		for (String line : lines) {
			String[] split = line.split("-");
			Cave left = caves.computeIfAbsent(split[0], name -> new Cave(name, new ArrayList<>()));
			Cave right = caves.computeIfAbsent(split[1], name -> new Cave(name, new ArrayList<>()));
			left.connections().add(right);
			right.connections().add(left);
		}
		return new CaveSystem(caves.get("start"), caves.get("end"), caves);
	}

	List<List<Cave>> pathsFromCaveToEnd(Cave current, List<Cave> pathToPrevious, boolean permitSingleSmallCaveTwice) {
		List<Cave> pathToCurrent = Stream.concat(pathToPrevious.stream(), Stream.of(current)).toList();
		if (current.equals(end)) {
			return List.of(pathToCurrent);
		}

		Map<String, Long> smallCavesVisited = pathToCurrent.stream()
				.filter(Cave::isSmall)
				.collect(groupingBy(Cave::name, counting()));
		boolean anySmallCaveVisitedTwice = smallCavesVisited.values().stream().anyMatch(visits -> 1 < visits);
		Set<String> reentryForbidden = permitSingleSmallCaveTwice && !anySmallCaveVisitedTwice ? Set.of(start.name()) : smallCavesVisited.keySet();
		return current.connections().stream()
				.filter(cave -> !reentryForbidden.contains(cave.name()))
				.flatMap(next -> pathsFromCaveToEnd(next, pathToCurrent, permitSingleSmallCaveTwice).stream())
				.toList();
	}

}

record Cave(String name, List<Cave> connections) implements Comparable<Cave> {
	boolean isSmall() {
		return name.equals(name.toLowerCase());
	}

	boolean isStart() {
		return name.equals("start");
	}

	boolean isEnd() {
		return name.equals("end");
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int compareTo(Cave o) {
		return name.compareTo(o.name);
	}
}
