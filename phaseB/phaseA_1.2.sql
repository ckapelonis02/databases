SELECT student.AM AS student_AM,
		person.name || ' ' || person.surname AS fullname,
		ROUND(SUM(course_weight(units)*final_grade) / SUM(course_weight(units)), 2)
			AS annual_grade,
		((EXTRACT(YEAR FROM semester.end_date)::INTEGER) - EXTRACT(YEAR FROM student.entry_date)::INTEGER) AS year
FROM "Student" student
	JOIN "Joins" joins ON ("StudentAMKA" = student.amka)
	JOIN "ProgramOffersCourse" offers ON (
		offers."ProgramID" = joins."ProgramID"
	)
	LEFT OUTER JOIN "Register" r ON (r.amka = "StudentAMKA" AND r.course_code = "CourseCode" AND
									r.register_status = 'pass')
	JOIN "CourseRun" crun ON (r.course_code = crun.course_code AND crun.serial_number = r.serial_number)
	JOIN "Semester" semester ON (semesterrunsin = semester_id AND
					(EXTRACT(YEAR FROM semester.end_date)::INTEGER) + 1 = (
					SELECT (EXTRACT(YEAR FROM end_date)::INTEGER)
					FROM "Semester"
					WHERE semester_status = 'present'
					LIMIT 1
					)
		)
	JOIN "Person" person ON (student.amka = person.amka)
	JOIN "Course" course ON (
		course.course_code = r.course_code
		AND typical_year = (
			CASE
			WHEN ((EXTRACT(YEAR FROM semester.end_date)::INTEGER) - EXTRACT(YEAR FROM student.entry_date)::INTEGER) > 5 THEN
				5
			ELSE
				((EXTRACT(YEAR FROM semester.end_date)::INTEGER) - EXTRACT(YEAR FROM student.entry_date)::INTEGER)
		 END
		)
	)
GROUP BY student.AM, person.name, person.surname, semester.end_date, student.entry_date
HAVING COUNT("CourseCode") = COUNT(r.course_code)			