package day12;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DayElevenTest {

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
		assertThat(distinctPathsThatVisitSmallCavesAtMostOnce(SAMPLE_ONE)).isEqualTo(10);
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
		assertThat(distinctPathsThatVisitSmallCavesAtMostOnce(SAMPLE_TWO)).isEqualTo(19);
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
		assertThat(distinctPathsThatVisitSmallCavesAtMostOnce(SAMPLE_THREE)).isEqualTo(226);
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
		assertThat(distinctPathsThatVisitSmallCavesAtMostOnce(INPUT)).isEqualTo(-1);
	}

	private static int distinctPathsThatVisitSmallCavesAtMostOnce(String input) {
		CaveSystem system = CaveSystem.parse(input);
		List<List<Cave>> paths = system.pathsFromCaveToEnd(system.start(), List.of());
		return paths.size();
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

	List<List<Cave>> pathsFromCaveToEnd(Cave current, List<Cave> pathToPrevious) {
		List<Cave> pathToCurrent = Stream.concat(pathToPrevious.stream(), Stream.of(current)).toList();
		if (current.equals(end)) {
			return List.of(pathToCurrent);
		}

		List<Cave> reentryForbidden = pathToCurrent.stream().filter(Cave::isSmall).toList();
		return current.connections().stream()
				.filter(Predicate.not(reentryForbidden::contains))
				.flatMap(next -> pathsFromCaveToEnd(next, pathToCurrent).stream())
				.toList();
	}

}

record Cave(String name, List<Cave> connections) {
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
}
