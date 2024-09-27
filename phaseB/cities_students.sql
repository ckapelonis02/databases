CREATE OR REPLACE VIEW city_entries AS
	SELECT person.city_id AS id
	FROM "Person" person
		JOIN "Student" student USING (amka)
		JOIN "Joins" joins ON (joins."StudentAMKA" = student.amka)
		JOIN "Program" program USING ("ProgramID")
	WHERE student.entry_date >= '2040-09-01'::date AND student.entry_date <= '2050-09-30'::date
		AND program."Duration" = 5
	GROUP BY student.amka, person.city_id
	HAVING COUNT(student.amka) >= 2;


SELECT name, COUNT(city_entries.id) AS student_num
FROM "Cities" LEFT OUTER JOIN city_entries USING (id)
WHERE population > 50000
GROUP BY id
ORDER BY student_num DESC;