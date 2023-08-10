package info.kgeorgiy.ja.kurkin.student;

import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.GroupQuery;
import info.kgeorgiy.java.advanced.student.Student;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class StudentDB implements GroupQuery {
    private <T> Stream<T> mappingType(Collection<Student> student, Function<Student, T> mapper) {
        return student.stream().map(mapper);
    }

    private <T> List<T> goToList(Stream<T> stream) {
        return stream.collect(Collectors.toList());
    }

    private List<Student> mappingTypeForSort(Collection<Student> students, Predicate<Student> predicate) {
        return students.stream().filter(predicate)
                .sorted(STUDENT_BY_NAME_COMP)
                .collect(Collectors.toList());
    }

    private final Comparator<Student> STUDENT_BY_NAME_COMP = Comparator
            .comparing(Student::getLastName)
            .thenComparing(Student::getFirstName)
            .reversed()
            .thenComparing(Student::getId);


    @Override
    public List<String> getFirstNames(List<Student> students) {
        return goToList(mappingType(students, Student::getFirstName));
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return goToList(mappingType(students, Student::getLastName));
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return goToList(mappingType(students, Student::getGroup));
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return goToList(mappingType(students, student -> student.getFirstName() + " " + student.getLastName()));
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return mappingType(students, Student::getFirstName).collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream()
                .max(Student::compareTo)
                .map(Student::getFirstName)
                .orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return students.stream()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return mappingTypeForSort(students, student -> true);
    }


    private <T> List<Student> forFind(Collection<Student> students, T name, Function<Student, T> p) {
        return mappingTypeForSort(students, student -> Objects.equals(p.apply(student), name));
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return forFind(students, name, Student::getFirstName);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return forFind(students, name, Student::getLastName);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return forFind(students, group, Student::getGroup);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return students.stream()
                .filter(student -> Objects.equals(student.getGroup(), group))
                .collect(Collectors.toMap(Student::getLastName,
                        Student::getFirstName, BinaryOperator.minBy(String::compareTo)));
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return groupSort(getGroupingBy(students)
                .map(student -> new Group(student.getKey(),
                        student.getValue().stream()
                                .sorted(STUDENT_BY_NAME_COMP)
                                .collect(Collectors.toList()))));

    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return groupSort(getGroupingBy(students)
                .map(student -> new Group(student.getKey(),
                        sortStudentsById(student.getValue())))
        );
    }

    @Override
    public GroupName getLargestGroup(Collection<Student> students) {
        return getGroupingBy(students)
                .max(Map.Entry.<GroupName, List<Student>>comparingByValue(Comparator.comparingInt(List::size))
                        .thenComparing(Map.Entry.comparingByKey()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    @Override
    public GroupName getLargestGroupFirstName(Collection<Student> students) {
        return students.stream()
                .collect(Collectors.groupingBy(Student::getGroup, Collectors.toList()))
                .entrySet().stream()
                .max(Map.Entry.<GroupName, List<Student>>comparingByValue(Comparator.comparingInt(g -> getDistinctFirstNames(g).size()))
                        .thenComparing(Collections.reverseOrder(Map.Entry.comparingByKey())))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private Stream<Map.Entry<GroupName, List<Student>>> getGroupingBy(Collection<Student> students) {
        return students.stream()
                .collect(Collectors.groupingBy(Student::getGroup, Collectors.toList()))
                .entrySet().stream();
    }

    private List<Group> groupSort(Stream<Group> students) {
        return students.sorted(Comparator.comparing(Group::getName))
                .toList();
    }
}
