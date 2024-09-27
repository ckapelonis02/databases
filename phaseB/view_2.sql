-- helpful function
-- returns weight given the units of a course
-- returns null for illegal units
CREATE OR REPLACE FUNCTION course_weight(units SMALLINT, OUT weight NUMERIC) AS
$$
BEGIN
	CASE
		WHEN units BETWEEN 1 AND 2 THEN
			weight := 1;
		WHEN units BETWEEN 3 AND 4 THEN
			weight := 1.5;
		WHEN units = 5 THEN
			weight := 2;
		ELSE
			weight := NULL;
	END CASE;
END;
$$ LANGUAGE 'plpgsql';

/*
	1.2. (*) Παρουσίαση του ετήσιου βαθμού των φοιτητών και του έτους φοίτησης.

	Για κάθε φοιτητή εμφανίζεται: ο αριθμός μητρώου, το ονοματεπώνυμο, ο ετήσιος
	βαθμός και το έτος σπουδών.

	Ο ετήσιος βαθμός ενός φοιτητή είναι ο μέσος όρος
	των βαθμών των μαθημάτων που έχει ολοκληρώσει επιτυχώς στο προηγούμενο ακαδημαϊκό
	έτος.

	Ο ετήσιος βαθμός υπολογίζεται μόνο για τους φοιτητές που έχουν ολοκληρώσει
	με επιτυχία όλα τα μαθήματα του προγράμματος σπουδών των εξαμήνων του προηγούμενου 
	ακαδημαϊκού έτους.

	Ο υπολογισμός είναι ανάλογος με αυτόν για το βαθμό διπλώματος
	(πολλαπλασιασμός κάθε βαθμού με το συντελεστή βαρύτητας του μαθήματος, άθροιση
	των επιμέρους γινομένων και διαίρεση με το άθροισμα των συντελεστών), ωστόσο 
	συμμετέχουν μόνο τα υποχρεωτικά και τα κατ’ επιλογήν υποχρεωτικά μαθήματα του
	προγράμματος σπουδών του προηγούμενου έτους, ενώ δεν συμμετέχουν τα επιπλέον
	μαθήματα που τυχόν ολοκλήρωσε ο φοιτητής.
*/
DROP VIEW student_annual_grade;
CREATE OR REPLACE VIEW student_annual_grade AS
	SELECT student.AM AS student_AM,
		person.name || ' ' || person.surname AS fullname,
		ROUND(SUM(course_weight(units)*final_grade) / SUM(course_weight(units)), 2)
			AS annual_grade,
		(semester.academic_year - EXTRACT(YEAR FROM student.entry_date)::INTEGER + 1) AS year
	FROM "Person" person
		JOIN "Student" student USING (amka)
		JOIN "Register" register USING (amka)
		JOIN "CourseRun" crun USING (course_code, serial_number)
		JOIN "Semester" semester ON (crun.semesterrunsin = semester.semester_id)
		JOIN "Joins" joins ON (joins."StudentAMKA" = register.amka)
		LEFT OUTER JOIN "ProgramOffersCourse" offers ON (offers."ProgramID" = joins."ProgramID"
			AND offers."CourseCode" = crun.course_code)
		JOIN "Course" course USING (course_code)
	WHERE semester.academic_year + 1 = (
		SELECT academic_year
		FROM "Semester"
		WHERE semester_status = 'present'
		LIMIT 1
		)
		AND (register_status = 'pass' OR register_status = 'fail')
	GROUP BY student.AM, person.name, person.surname, semester.academic_year, student.entry_date
	HAVING SUM(CASE WHEN register.register_status = 'fail' THEN 1 ELSE 0 END) = 0;





-- SELECT new_program(
-- 	'typical'::program_type,
-- 	'dontcare'::VARCHAR,
-- 	'dontcare'::VARCHAR,
-- 	'2012'::CHARACTER(4),
-- 	5::INTEGER,
-- 	20,
-- 	40,
-- 	FALSE,
-- 	3,
-- 	'degree'::diploma_type
-- );

-- SELECT * FROM student_annual_grade;