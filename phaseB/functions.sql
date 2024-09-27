/*
	helpful functions managing random data configuration
*/

CREATE OR REPLACE FUNCTION random_string(length INTEGER, greek BOOLEAN) RETURNS TEXT AS $$
DECLARE
	chars TEXT[];
	result TEXT := '';
	i INTEGER := 0;
BEGIN
	IF (greek) THEN
		chars := '{Α,Β,Γ,Δ,Ε,Ζ,Η,Θ,Ι,Κ,Λ,Μ,Ν,Ξ,Ο,Π,Ρ,Σ,Τ,Υ,Φ,Χ,Ψ,Ω}';
	ELSE
		chars := '{a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z}';
	END IF;

	IF length < 0 THEN
		RAISE EXCEPTION 'Given length cannot be less than 0';
	END IF;

	FOR i IN 1..length LOOP
		result := result || chars[RANDOM()*(ARRAY_LENGTH(chars, 1) - 1) + 1];
	END LOOP;
	RETURN result;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION random_date(start_date DATE, end_date DATE)
	RETURNS DATE AS $$
DECLARE
	random_days INTEGER;
BEGIN
	SELECT (random() * (end_date - start_date + 1))::INTEGER INTO random_days;
	RETURN start_date + random_days;
END;
$$ LANGUAGE plpgsql;














-- inserting n programs
-- we set sys_data = 'rand' so that we know which insertions we just made
CREATE OR REPLACE FUNCTION insert_into_program_table(n INTEGER) RETURNS VOID AS $$
DECLARE
	i INTEGER;
	max_id INTEGER;
BEGIN
	SELECT MAX("ProgramID") INTO max_id FROM "Program";
	IF max_id IS NULL THEN
		max_id := 0;
	END IF;
	FOR i IN 1..n LOOP
		INSERT INTO public."Program" ("ProgramID", "Duration", "MinCourses", "MinCredits", "Obligatory", "CommitteeNum", "DiplomaType", "NumOfParticipants", "Year", sys_data)
		VALUES (i + max_id, MOD(i, 4) + 3, 50, 300, TRUE, 3, 'diploma'::diploma_type, 0, '2023', 'rand');
	END LOOP;
END;
$$ LANGUAGE plpgsql;



-- inserting n persons, m of those are students (ASSERT(n >= m))
-- we set sys_data = 'rand' so that we know which insertions we just made
CREATE OR REPLACE FUNCTION insert_people(s INTEGER, p INTEGER) RETURNS VOID AS $$
DECLARE
	i INTEGER;
	max_amka BIGINT;
	max_am BIGINT;
	city_ids INTEGER[] := (SELECT ARRAY(SELECT id FROM "Cities"));
BEGIN
	IF (s > p) THEN
		RETURN;
	END IF;

	SELECT MAX(amka)::BIGINT INTO max_amka FROM "Person";
	IF max_amka IS NULL THEN
		max_amka := 0;
	END IF;

	SELECT MAX(am)::BIGINT INTO max_am FROM "Student";
	IF max_am IS NULL THEN
		max_am := 0;
	END IF;

	FOR i IN 1..p LOOP
		INSERT INTO public."Person" (
			amka, 
			name, 
			father_name, 
			surname, 
			email, 
			city_id, 
			sys_data
		)
		VALUES (
			(max_amka + 1 + i)::VARCHAR,
			random_string(10, TRUE),
			random_string(10, TRUE),
			random_string(10, TRUE),
			random_string(5, FALSE) || '@tuc.gr',
			city_ids[RANDOM()*(ARRAY_LENGTH(city_ids, 1) - 1) + 1],
			'rand'
		);

		IF (i <= s) THEN
			INSERT INTO public."Student" (
				amka,
				am,
				entry_date,
				sys_data
			)
			VALUES (
				(max_amka + 1 + i)::VARCHAR,
				(max_am + 1 + i)::CHARACTER(10),
				random_date('2020-01-01'::DATE, '2050-12-31'::DATE),
				'rand'
			);
		END IF;

	END LOOP;
END;
$$ LANGUAGE plpgsql;





-- inserting random joins from n random students to p random (randomly determined) programs
-- we set sys_data = 'rand' so that we know which insertions in joins we just made
CREATE OR REPLACE FUNCTION insert_random_joins(n INTEGER, p INTEGER) RETURNS VOID AS $$
DECLARE
	i INTEGER;
	programs INTEGER[] := (
		SELECT ARRAY(
			SELECT "ProgramID"
			FROM "Program"
			ORDER BY RANDOM()
			LIMIT p
		)
	);
BEGIN
	INSERT INTO public."Joins" ("StudentAMKA", "ProgramID", "sys_data")
	SELECT amka, programs[CEILING(RANDOM()*p)], 'rand'
	FROM "Student"
	LIMIT n;
END;
$$ LANGUAGE plpgsql;








/*
	examples of insertions
*/
-- SELECT insert_into_program_table(300000);
-- SELECT insert_people(500000, 700000);
-- SELECT insert_random_joins(400000, 100);