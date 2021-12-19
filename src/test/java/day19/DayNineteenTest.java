package day19;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

class DayNineteenTest {

	@Test
	void partOneSample() throws Exception {
		String sample = Files.readString(Paths.get(getClass().getResource("sample").toURI()));
		assertThat(countDistinctBeaconsInFullMap(sample).size()).isEqualTo(79);
	}

	private static List<Position> countDistinctBeaconsInFullMap(String input) {
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

	public static List<Position> assembleUniqueBeaconsRelativeToFirstScanner(List<Scanner> scanners) {
		Scanner zero = scanners.get(0);

		Stream<ScannerPair> overlappingScannerPairs = overlappingScannerPairs(scanners);

		return List.of();// TODO
	}

	static Stream<ScannerPair> overlappingScannerPairs(List<Scanner> scanners) {
		return scanners.stream()
				.flatMap(left -> scanners.stream()
						.flatMap(right -> right.orientations().stream())
						.map(orientation -> new ScannerPair(left, orientation)))
				.filter(pair -> pair.left().id() < pair.right().id())
				.filter(ScannerPair::overlap);
	}

}

record ScannerPair(Scanner left, Scanner right) {

	boolean overlap() {
		Set<Position> intersection = left.distancePairs();
		intersection.retainAll(right.distancePairs());
		return 12 < intersection.size();
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

	public Set<Position> distancePairs() {
		return beacons.stream()
				.flatMap(left -> beacons.stream()
						.filter(right -> left.compareTo(right) < 0)
						.map(right -> new PositionPair(left, right)))
				.map(PositionPair::distance)
				.collect(toSet());
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
				left.x() - right.x(),
				left.y() - right.y(),
				left.z() - right.z());
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
