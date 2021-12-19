package day19;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

class DayNineteenTest {

	@Test
	void partOneSample() throws Exception {
		String sample = Files.readString(Paths.get(getClass().getResource("sample").toURI()));
		String beacons = Files.readString(Paths.get(getClass().getResource("beacons").toURI()));
		assertThat(countDistinctBeaconsInFullMap(sample).map(Position::toString).collect(joining("\n")))
				.isEqualTo(beacons);
	}

	private static Stream<Position> countDistinctBeaconsInFullMap(String input) {
		List<Scanner> scanners = Parser.parse(input);
		return Mapper.assembleUniqueBeaconsRelativeToFirstScanner(scanners);
	}

	@Test
	void parserTest() throws Exception {
		String sample = Files.readString(Paths.get(getClass().getResource("sample").toURI()));
		String restored = Parser.parse(sample).stream().map(Scanner::toString).collect(joining("\n"));
		assertThat(restored).isEqualTo(sample);
	}

	@Test
	void orientationTest() throws Exception {
		String sample = Files.readString(Paths.get(getClass().getResource("orientations").toURI()));
		List<Scanner> orientations = Parser.parse(sample);
		assertThat(orientations.get(0).orientations())
				.hasSize(24)
				.containsAll(orientations);
	}

	@Test
	void overlappingTest() throws Exception {
		String sample = Files.readString(Paths.get(getClass().getResource("sample").toURI()));
		List<Scanner> scanners = Parser.parse(sample);
		String overlapping = Mapper.overlappingScannerPairs(scanners).map(ScannerPair::toString).collect(joining(", "));
		assertThat(overlapping).isEqualTo("0-1, 0-4, 1-2, 1-3, 1-4, 2-4, 3-4");
	}

}

class Parser {

	public static List<Scanner> parse(String input) {
		return Stream.of(input.split("\n\n")).map(Parser::scanner).toList();
	}

	private static Scanner scanner(String block) {
		String[] lines = block.split("\n");
		int id = Integer.parseInt(lines[0].split(" ")[2]);
		return new Scanner(id, Stream.of(lines).skip(1).map(Parser::position).toList());
	}

	private static Position position(String line) {
		int[] values = Stream.of(line.split(",")).mapToInt(Integer::parseInt).toArray();
		return new Position(values[0], values[1], values[2]);
	}

}

class Mapper {

	public static Stream<Position> assembleUniqueBeaconsRelativeToFirstScanner(List<Scanner> scanners) {
		Stream<ScannerPair> overlapping = overlappingScannerPairs(scanners);
		Map<Integer, List<ScannerPair>> grouped = overlapping.collect(Collectors.groupingBy(pair -> pair.left().id()));

		Scanner map = new Scanner(-1, new ArrayList<>());
		map.beacons().addAll(grouped.get(0).stream()
				.flatMap(ScannerPair::beaconsViewedFromLeft)
				.toList());

//		List<ScannerPair> overlappingScannerPairs = overlapping.toList();
//		for (ScannerPair pair : overlappingScannerPairs) {
//			map.beacons().addAll(
//					overlappingScannerPairs(map, List.of(pair.left(), pair.right()))
//							.map(ScannerPair::right)
//							.map(Scanner::beacons)
//							.flatMap(List::stream)
//							.toList());
//		}
		return map.beacons().stream().distinct().sorted();
	}

	static Stream<ScannerPair> overlappingScannerPairs(List<Scanner> scanners) {
		return scanners.stream().flatMap(left -> overlappingScannerPairs(left, scanners));
	}

	private static Stream<ScannerPair> overlappingScannerPairs(Scanner left, List<Scanner> scanners) {
		return scanners.stream()
				.flatMap(right -> right.orientations().stream())
				.map(orientation -> new ScannerPair(left, orientation))
				.filter(pair -> pair.left().id() < pair.right().id())
				.filter(ScannerPair::overlap);
	}

}

record ScannerPair(Scanner left, Scanner right) {

	boolean overlap() {
		Set<Position> intersection = left.distancePairs().keySet();
		intersection.retainAll(right.distancePairs().keySet());
		return 12 < intersection.size();
	}

	Stream<Position> beaconsViewedFromLeft() {
		// overlap established these scanners contains more than twelve overlapping distances between pairs of beacons
		Map<Position, PositionPair> leftDistancePairs = left.distancePairs();
		Map<Position, PositionPair> rightDistancePairs = left.distancePairs();
		Position delta = rightDistancePairs.entrySet().stream()
				.filter(entry -> leftDistancePairs.containsKey(entry.getKey()))
				.findFirst()
				.map(entry -> new PositionPair(
						leftDistancePairs.get(entry.getKey()).left(),
						entry.getValue().left()))
				.map(PositionPair::distance)
				.get();

		// TODO This is incorrect
		Stream<Position> rightAdjusted = right.beacons().stream().map(p -> new Position(
				p.x() - delta.x(),
				p.y() - delta.y(),
				p.z() - delta.z()));

		return Stream.concat(left.beacons().stream(), rightAdjusted).distinct().sorted();
	}

	@Override
	public String toString() {
		return "%d-%d".formatted(left.id(), right.id());
	}

}

record Scanner(int id, List<Position> beacons) {

	public Set<Scanner> orientations() {
		return Stream.of(this, this.flip())
				.flatMap(scanner -> Stream.iterate(scanner, Scanner::clockwiseTurn).limit(4))
				.flatMap(scanner -> Stream.iterate(scanner, Scanner::spin).limit(3))
				.collect(toSet());
	}

	private Scanner clockwiseTurn() {
		return new Scanner(id, beacons.stream().map(p -> new Position(p.y(), -p.x(), p.z())).toList());
	}

	private Scanner spin() {
		return new Scanner(id, beacons.stream().map(p -> new Position(p.y(), p.z(), p.x())).toList());
	}

	private Scanner flip() {
		return new Scanner(id, beacons.stream().map(p -> new Position(p.x(), -p.y(), -p.z())).toList());
	}

	public Map<Position, PositionPair> distancePairs() {
		return beacons.stream()
				.flatMap(left -> beacons.stream()
						.filter(right -> left.compareTo(right) < 0)
						.map(right -> new PositionPair(left, right)))
				.collect(toMap(PositionPair::distance, Function.identity()));
	}

	@Override
	public String toString() {
		return """
				--- scanner %d ---
				%s
				""".formatted(id, beacons.stream().map(Position::toString).collect(joining("\n")));
	}

}

record PositionPair(Position left, Position right) {

	Position distance() {
		return new Position(
				right.x() - left.x(),
				right.y() - left.y(),
				right.z() - left.z());
	}

}

record Position(int x, int y, int z) implements Comparable<Position> {

	@Override
	public String toString() {
		return "%d,%d,%d".formatted(x, y, z);
	}

	@Override
	public int compareTo(Position other) {
		return comparing(Position::x)
				.thenComparing(Position::y)
				.thenComparing(Position::z)
				.compare(this, other);
	}

}
