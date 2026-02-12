--
-- PostgreSQL database dump
--

\restrict 9SsoNr18iCh53m7cipdmLc8QbPq8WzAt7gcJCS1Tg7CPwyqfbt2Z94vSL1xHSuq

-- Dumped from database version 15.15
-- Dumped by pg_dump version 15.15

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: allocation_type; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.allocation_type AS ENUM (
    'PROJECT',
    'PROSPECT',
    'VACATION',
    'MATERNITY'
);


--
-- Name: allocationtype; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.allocationtype AS ENUM (
    'PROJECT',
    'PROSPECT',
    'VACATION',
    'MATERNITY'
);


--
-- Name: gender; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.gender AS ENUM (
    'MALE',
    'FEMALE'
);


--
-- Name: gender_type; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.gender_type AS ENUM (
    'MALE',
    'FEMALE'
);


--
-- Name: hiring_type; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.hiring_type AS ENUM (
    'FULL_TIME',
    'PART_TIME'
);


--
-- Name: hiringtype; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.hiringtype AS ENUM (
    'FULL_TIME',
    'PART_TIME'
);


--
-- Name: job_level_type; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.job_level_type AS ENUM (
    'ENTRY_LEVEL',
    'MID_LEVEL',
    'ADVANCED_MANAGER_LEVEL',
    'EXECUTIVE_LEVEL'
);


--
-- Name: joblevel; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.joblevel AS ENUM (
    'ENTRY_LEVEL',
    'MID_LEVEL',
    'ADVANCED_MANAGER_LEVEL',
    'EXECUTIVE_LEVEL'
);


--
-- Name: project_status_type; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.project_status_type AS ENUM (
    'ACTIVE',
    'COMPLETED',
    'ON_HOLD'
);


--
-- Name: project_type; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.project_type AS ENUM (
    'PROJECT',
    'OPPORTUNITY'
);


--
-- Name: projectstatus; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.projectstatus AS ENUM (
    'ACTIVE',
    'COMPLETED',
    'ON_HOLD'
);


--
-- Name: projecttype; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.projecttype AS ENUM (
    'PROJECT',
    'OPPORTUNITY'
);


--
-- Name: skill_grade_type; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.skill_grade_type AS ENUM (
    'ADVANCED',
    'INTERMEDIATE',
    'BEGINNER'
);


--
-- Name: skill_level_type; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.skill_level_type AS ENUM (
    'PRIMARY',
    'SECONDARY'
);


--
-- Name: skillgrade; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.skillgrade AS ENUM (
    'ADVANCED',
    'INTERMEDIATE',
    'BEGINNER'
);


--
-- Name: skilllevel; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.skilllevel AS ENUM (
    'PRIMARY',
    'SECONDARY'
);


--
-- Name: CAST (public.allocationtype AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.allocationtype AS character varying) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (public.gender AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.gender AS character varying) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (public.hiringtype AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.hiringtype AS character varying) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (public.joblevel AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.joblevel AS character varying) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (public.projectstatus AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.projectstatus AS character varying) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (public.projecttype AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.projecttype AS character varying) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (public.skillgrade AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.skillgrade AS character varying) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (public.skilllevel AS character varying); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (public.skilllevel AS character varying) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (character varying AS public.allocationtype); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.allocationtype) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (character varying AS public.gender); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.gender) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (character varying AS public.hiringtype); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.hiringtype) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (character varying AS public.joblevel); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.joblevel) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (character varying AS public.projectstatus); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.projectstatus) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (character varying AS public.projecttype); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.projecttype) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (character varying AS public.skillgrade); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.skillgrade) WITH INOUT AS IMPLICIT;


--
-- Name: CAST (character varying AS public.skilllevel); Type: CAST; Schema: -; Owner: -
--

CREATE CAST (character varying AS public.skilllevel) WITH INOUT AS IMPLICIT;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: allocations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.allocations (
    id bigint NOT NULL,
    allocation_type public.allocation_type,
    end_date date,
    start_date date,
    employee_id bigint NOT NULL,
    project_id bigint
);


--
-- Name: allocations_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.allocations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: allocations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.allocations_id_seq OWNED BY public.allocations.id;


--
-- Name: employees; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employees (
    id bigint NOT NULL,
    cost_center character varying(255),
    email character varying(255),
    gender public.gender_type,
    grade character varying(255),
    hire_date date,
    hiring_type public.hiring_type,
    job_level public.job_level_type,
    legal_entity character varying(255),
    location character varying(255),
    name character varying(255) NOT NULL,
    nationality character varying(255),
    oracle_id integer,
    reason_of_leave character varying(255),
    resignation_date date,
    title character varying(255),
    manager_id bigint,
    tower integer
);


--
-- Name: employees_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.employees_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: employees_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.employees_id_seq OWNED BY public.employees.id;


--
-- Name: employees_skills; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employees_skills (
    id integer NOT NULL,
    skill_grade public.skill_grade_type,
    skill_level public.skill_level_type,
    employee_id bigint,
    skill_id integer
);


--
-- Name: employees_skills_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.employees_skills_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: employees_skills_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.employees_skills_id_seq OWNED BY public.employees_skills.id;


--
-- Name: monthly_allocations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.monthly_allocations (
    id bigint NOT NULL,
    month integer NOT NULL,
    percentage integer NOT NULL,
    year integer NOT NULL,
    allocation_id bigint NOT NULL
);


--
-- Name: monthly_allocations_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.monthly_allocations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: monthly_allocations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.monthly_allocations_id_seq OWNED BY public.monthly_allocations.id;


--
-- Name: projects; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.projects (
    id bigint NOT NULL,
    description character varying(255),
    end_date date,
    project_id character varying(255),
    project_type public.project_type,
    region character varying(255),
    start_date date,
    status public.project_status_type,
    vertical character varying(255),
    manager_id bigint
);


--
-- Name: projects_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.projects_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: projects_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.projects_id_seq OWNED BY public.projects.id;


--
-- Name: skills; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.skills (
    id integer NOT NULL,
    description character varying(255),
    tower_id integer
);


--
-- Name: skills_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.skills_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: skills_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.skills_id_seq OWNED BY public.skills.id;


--
-- Name: tech_towers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tech_towers (
    id integer NOT NULL,
    description character varying(255),
    parent_tower_id integer
);


--
-- Name: tech_towers_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tech_towers_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tech_towers_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tech_towers_id_seq OWNED BY public.tech_towers.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    email character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    username character varying(255) NOT NULL,
    employee_id bigint NOT NULL
);


--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: allocations id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.allocations ALTER COLUMN id SET DEFAULT nextval('public.allocations_id_seq'::regclass);


--
-- Name: employees id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employees ALTER COLUMN id SET DEFAULT nextval('public.employees_id_seq'::regclass);


--
-- Name: employees_skills id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employees_skills ALTER COLUMN id SET DEFAULT nextval('public.employees_skills_id_seq'::regclass);


--
-- Name: monthly_allocations id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.monthly_allocations ALTER COLUMN id SET DEFAULT nextval('public.monthly_allocations_id_seq'::regclass);


--
-- Name: projects id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.projects ALTER COLUMN id SET DEFAULT nextval('public.projects_id_seq'::regclass);


--
-- Name: skills id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.skills ALTER COLUMN id SET DEFAULT nextval('public.skills_id_seq'::regclass);


--
-- Name: tech_towers id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tech_towers ALTER COLUMN id SET DEFAULT nextval('public.tech_towers_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Data for Name: allocations; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.allocations (id, allocation_type, end_date, start_date, employee_id, project_id) FROM stdin;
1	PROJECT	2026-06-11	2025-12-11	22	2
2	PROJECT	2026-05-11	2025-09-11	23	5
3	PROJECT	2026-02-11	2025-09-11	24	14
4	PROJECT	2026-04-11	2026-02-11	24	13
5	PROJECT	2026-03-11	2026-02-11	25	9
6	PROJECT	2026-06-11	2025-09-11	26	4
7	PROJECT	2026-03-11	2025-12-11	26	3
8	PROJECT	2026-06-11	2026-01-11	27	7
9	PROJECT	2026-05-11	2026-02-11	28	7
10	PROJECT	2026-05-11	2025-12-11	28	9
11	PROJECT	2026-06-11	2025-10-11	29	13
12	PROJECT	2026-04-11	2025-11-11	30	1
13	PROJECT	2026-07-11	2025-10-11	31	10
14	PROJECT	2026-04-11	2026-01-11	31	15
15	PROJECT	2026-06-11	2026-01-11	32	10
16	PROJECT	2026-03-11	2025-12-11	33	14
17	PROJECT	2026-03-11	2025-11-11	34	2
18	PROJECT	2026-07-11	2025-12-11	35	5
19	PROJECT	2026-05-11	2025-09-11	35	15
20	PROJECT	2026-05-11	2026-01-11	36	3
21	PROJECT	2026-04-11	2026-01-11	36	15
22	PROJECT	2026-02-11	2026-01-11	37	6
23	PROJECT	2026-04-11	2026-01-11	38	7
24	PROJECT	2026-03-11	2025-12-11	38	13
25	PROJECT	2026-02-11	2025-10-11	39	5
26	PROJECT	2026-03-11	2026-02-11	40	11
27	PROJECT	2026-02-11	2025-09-11	40	5
28	PROJECT	2026-05-11	2025-09-11	41	10
29	PROJECT	2026-04-11	2025-10-11	41	2
30	PROJECT	2026-07-11	2025-11-11	42	7
31	PROJECT	2026-04-11	2026-01-11	42	14
32	PROJECT	2026-02-11	2025-10-11	43	9
33	PROJECT	2026-06-11	2026-01-11	43	8
34	PROJECT	2026-05-11	2025-12-11	44	11
35	PROJECT	2026-05-11	2025-09-11	45	8
36	PROJECT	2026-06-11	2025-10-11	46	4
37	PROJECT	2026-02-11	2025-09-11	46	7
38	PROJECT	2026-03-11	2025-11-11	47	4
39	PROJECT	2026-03-11	2025-11-11	47	7
40	PROJECT	2026-05-11	2025-10-11	48	2
41	PROJECT	2026-06-11	2026-01-11	48	9
42	PROJECT	2026-03-11	2025-09-11	49	4
43	PROJECT	2026-05-11	2025-09-11	50	11
44	PROJECT	2026-04-11	2025-09-11	51	7
45	PROJECT	2026-02-11	2026-01-11	51	10
46	PROJECT	2026-05-11	2026-01-11	52	15
47	PROJECT	2026-03-11	2025-10-11	52	8
48	PROJECT	2026-04-11	2026-02-11	53	7
49	PROJECT	2026-04-11	2026-01-11	54	11
50	PROJECT	2026-02-11	2025-09-11	55	16
51	PROJECT	2026-07-11	2025-09-11	56	15
52	PROJECT	2026-02-11	2025-11-11	57	7
53	PROJECT	2026-02-11	2025-11-11	58	2
54	PROJECT	2026-06-11	2025-12-11	59	4
55	PROJECT	2026-04-11	2025-11-11	60	15
56	PROJECT	2026-05-11	2025-09-11	60	10
57	PROJECT	2026-06-11	2025-11-11	61	5
58	PROJECT	2026-03-11	2025-12-11	62	14
59	PROJECT	2026-06-11	2025-10-11	63	6
60	PROJECT	2026-03-11	2026-02-11	64	13
61	PROJECT	2026-04-11	2025-12-11	64	2
62	PROJECT	2026-05-11	2026-02-11	65	11
63	PROJECT	2026-04-11	2025-10-11	66	10
64	PROJECT	2026-04-11	2025-10-11	67	10
65	PROJECT	2026-07-11	2025-12-11	68	7
66	PROJECT	2026-06-11	2025-11-11	69	14
67	PROJECT	2026-04-11	2026-01-11	69	4
68	PROJECT	2026-07-11	2026-02-11	70	14
69	PROJECT	2026-05-11	2025-10-11	71	10
70	PROJECT	2026-03-11	2025-12-11	72	10
71	PROJECT	2026-04-11	2026-02-11	73	7
72	PROJECT	2026-06-11	2026-02-11	74	2
73	PROJECT	2026-02-11	2025-10-11	74	14
74	PROJECT	2026-07-11	2026-01-11	75	7
75	PROJECT	2026-03-11	2025-10-11	76	14
76	PROJECT	2026-02-11	2025-12-11	77	13
77	PROJECT	2026-07-11	2025-10-11	78	2
78	PROJECT	2026-04-11	2025-09-11	78	7
79	PROJECT	2026-04-11	2026-02-11	79	12
80	PROJECT	2026-06-11	2025-12-11	80	5
81	PROJECT	2026-07-11	2025-09-11	80	14
82	PROJECT	2026-07-11	2025-09-11	81	12
83	PROJECT	2026-05-11	2025-12-11	82	1
84	PROJECT	2026-07-11	2025-12-11	82	4
85	PROJECT	2026-04-11	2025-12-11	83	9
86	PROJECT	2026-06-11	2026-01-11	83	12
87	PROJECT	2026-03-11	2025-09-11	84	4
88	PROJECT	2026-03-11	2026-01-11	85	10
89	PROJECT	2026-02-11	2025-09-11	85	3
90	PROJECT	2026-04-11	2025-12-11	86	4
91	PROJECT	2026-07-11	2026-01-11	87	9
92	PROJECT	2026-06-11	2025-12-11	87	4
93	PROJECT	2026-03-11	2025-12-11	88	1
94	PROJECT	2026-04-11	2026-01-11	89	9
95	PROJECT	2026-03-11	2025-11-11	89	6
96	PROJECT	2026-07-11	2025-12-11	90	2
97	PROJECT	2026-04-11	2025-12-11	90	3
98	PROJECT	2026-06-11	2026-02-11	91	2
99	PROJECT	2026-06-11	2025-10-11	92	7
100	PROJECT	2026-05-11	2026-01-11	92	11
101	PROJECT	2026-07-11	2025-10-11	93	8
102	PROJECT	2026-04-11	2025-11-11	94	3
103	PROJECT	2026-02-11	2025-12-11	94	10
104	PROJECT	2026-03-11	2026-01-11	95	12
105	PROJECT	2026-04-11	2025-09-11	96	16
106	PROJECT	2026-05-11	2025-09-11	97	8
107	PROJECT	2026-07-11	2025-11-11	97	15
108	PROJECT	2026-06-11	2025-12-11	98	5
109	PROJECT	2026-02-11	2026-01-11	98	12
110	PROJECT	2026-06-11	2026-01-11	99	6
111	PROJECT	2026-07-11	2026-01-11	99	9
112	PROJECT	2026-03-11	2025-10-11	100	1
113	PROJECT	2026-03-11	2025-10-11	101	10
114	PROJECT	2026-05-11	2026-01-11	102	8
115	PROJECT	2026-07-11	2025-12-11	102	5
116	PROJECT	2026-07-11	2026-02-11	103	3
117	PROJECT	2026-05-11	2026-02-11	104	2
118	PROJECT	2026-07-11	2026-01-11	105	15
119	PROJECT	2026-05-11	2026-02-11	105	6
120	PROJECT	2026-07-11	2026-01-11	106	5
121	PROJECT	2026-06-11	2025-09-11	106	6
122	PROJECT	2026-02-11	2025-11-11	107	16
123	PROJECT	2026-05-11	2026-02-11	107	6
124	PROJECT	2026-02-11	2025-11-11	108	14
125	PROJECT	2026-04-11	2025-11-11	108	4
126	PROJECT	2026-06-11	2026-02-11	109	1
127	PROJECT	2026-05-11	2025-11-11	110	16
128	PROJECT	2026-04-11	2025-09-11	110	9
129	PROJECT	2026-04-11	2026-01-11	111	2
130	PROJECT	2026-06-11	2025-12-11	112	8
131	PROJECT	2026-05-11	2025-12-11	113	11
132	PROJECT	2026-03-11	2025-09-11	114	7
133	PROJECT	2026-05-11	2026-01-11	114	12
134	PROJECT	2026-02-11	2025-12-11	115	5
135	PROJECT	2026-07-11	2025-11-11	115	9
136	PROJECT	2026-05-11	2025-09-11	116	1
137	PROJECT	2026-02-11	2025-09-11	117	8
138	PROJECT	2026-07-11	2025-10-11	117	16
139	PROJECT	2026-06-11	2025-10-11	118	13
140	PROJECT	2026-05-11	2026-01-11	119	2
141	PROJECT	2026-02-11	2026-02-11	119	5
142	PROJECT	2026-05-11	2025-12-11	120	8
143	PROJECT	2026-03-11	2025-09-11	121	9
144	PROJECT	2026-07-11	2026-01-11	122	14
145	PROJECT	2026-05-11	2025-10-11	122	6
146	PROJECT	2026-04-11	2025-09-11	123	13
147	PROJECT	2026-05-11	2025-09-11	124	12
148	PROJECT	2026-04-11	2025-10-11	124	2
149	PROJECT	2026-07-11	2026-02-11	125	6
150	PROJECT	2026-05-11	2025-10-11	125	8
151	PROJECT	2026-02-11	2025-10-11	126	4
152	PROJECT	2026-03-11	2025-11-11	127	13
153	PROJECT	2026-02-11	2025-10-11	127	6
154	PROJECT	2026-03-11	2025-10-11	128	2
155	PROJECT	2026-04-11	2026-02-11	129	6
156	PROJECT	2026-06-11	2025-12-11	130	11
157	PROJECT	2026-05-11	2025-12-11	130	15
158	PROJECT	2026-06-11	2025-10-11	131	16
159	PROJECT	2026-03-11	2025-11-11	131	8
160	PROJECT	2026-03-11	2025-11-11	132	2
161	PROJECT	2026-07-11	2025-12-11	133	10
162	PROJECT	2026-05-11	2025-11-11	134	9
163	PROJECT	2026-05-11	2026-01-11	134	14
164	PROJECT	2026-04-11	2025-11-11	135	9
165	PROJECT	2026-05-11	2026-02-11	136	6
166	PROJECT	2026-05-11	2025-11-11	136	9
167	PROJECT	2026-06-11	2026-02-11	137	1
168	PROJECT	2026-07-11	2026-02-11	137	6
169	PROJECT	2026-02-11	2026-02-11	138	2
170	PROJECT	2026-06-11	2026-02-11	138	3
171	PROJECT	2026-03-11	2025-12-11	139	10
172	PROJECT	2026-03-11	2025-10-11	139	14
173	PROJECT	2026-06-11	2025-12-11	140	6
174	PROJECT	2026-02-11	2026-01-11	140	16
175	PROJECT	2026-07-11	2025-11-11	141	2
176	PROJECT	2026-05-11	2025-11-11	142	10
177	PROJECT	2026-04-11	2025-10-11	142	7
178	PROJECT	2026-06-11	2025-12-11	143	14
179	PROJECT	2026-03-11	2026-01-11	144	15
180	PROJECT	2026-07-11	2025-10-11	144	6
181	PROJECT	2026-03-11	2026-02-11	145	5
182	PROJECT	2026-07-11	2026-02-11	146	12
183	PROJECT	2026-05-11	2025-10-11	147	11
184	PROJECT	2026-03-11	2026-01-11	147	5
185	PROJECT	2026-04-11	2025-11-11	148	3
186	PROJECT	2026-04-11	2025-12-11	149	16
187	PROJECT	2026-02-11	2025-11-11	150	2
188	PROJECT	2026-07-11	2026-02-11	151	1
189	PROJECT	2026-03-11	2025-10-11	152	7
190	PROJECT	2026-04-11	2026-01-11	153	2
191	PROJECT	2026-03-11	2025-11-11	154	15
192	PROJECT	2026-06-11	2026-01-11	154	7
193	PROJECT	2026-07-11	2025-10-11	155	3
194	PROJECT	2026-04-11	2025-11-11	155	5
195	PROJECT	2026-05-11	2025-10-11	156	12
196	PROJECT	2026-05-11	2025-11-11	156	11
197	PROJECT	2026-03-11	2026-02-11	157	6
198	PROJECT	2026-07-11	2026-02-11	158	4
199	PROJECT	2026-06-11	2025-12-11	158	5
200	PROJECT	2026-07-11	2025-09-11	159	7
201	PROJECT	2026-03-11	2025-12-11	159	16
202	PROJECT	2026-06-11	2025-12-11	160	6
203	PROJECT	2026-03-11	2026-01-11	160	11
204	PROJECT	2026-04-11	2025-10-11	161	12
205	PROJECT	2026-07-11	2025-11-11	162	12
206	PROJECT	2026-05-11	2025-12-11	163	12
207	PROJECT	2026-02-11	2026-01-11	163	7
208	PROJECT	2026-02-11	2026-01-11	164	5
209	PROJECT	2026-04-11	2025-11-11	164	9
210	PROJECT	2026-07-11	2025-12-11	165	3
211	PROJECT	2026-07-11	2026-02-11	165	1
212	PROJECT	2026-06-11	2025-11-11	166	16
213	PROJECT	2026-06-11	2025-09-11	166	1
214	PROJECT	2026-02-11	2025-10-11	167	13
215	PROJECT	2026-04-11	2025-12-11	167	4
216	PROJECT	2026-05-11	2025-12-11	168	1
217	PROJECT	2026-05-11	2025-11-11	169	2
218	PROJECT	2026-03-11	2026-01-11	170	1
219	PROJECT	2026-06-11	2026-02-11	170	12
220	PROJECT	2026-03-11	2026-02-11	171	11
221	PROJECT	2026-06-11	2026-02-11	171	4
222	PROJECT	2026-07-11	2026-02-11	172	9
223	PROJECT	2026-05-11	2025-11-11	173	13
224	PROJECT	2026-04-11	2025-12-11	174	8
225	PROJECT	2026-07-11	2026-01-11	174	10
226	PROJECT	2026-07-11	2025-11-11	175	1
227	PROJECT	2026-06-11	2026-01-11	176	12
228	PROJECT	2026-07-11	2025-10-11	177	14
229	PROJECT	2026-02-11	2025-11-11	178	5
230	PROJECT	2026-07-11	2025-12-11	178	6
231	PROJECT	2026-02-11	2026-02-11	179	6
232	PROJECT	2026-04-11	2026-01-11	180	16
233	PROJECT	2026-02-11	2025-12-11	180	11
234	PROJECT	2026-02-11	2026-02-11	181	7
235	PROJECT	2026-06-11	2025-09-11	182	8
236	PROJECT	2026-04-11	2026-02-11	182	7
237	PROJECT	2026-03-11	2026-02-11	183	11
238	PROJECT	2026-06-11	2025-12-11	184	7
239	PROJECT	2026-02-11	2025-11-11	185	12
240	PROJECT	2026-05-11	2026-02-11	186	8
241	PROJECT	2026-06-11	2025-11-11	187	15
242	PROJECT	2026-02-11	2025-11-11	187	9
243	PROJECT	2026-05-11	2026-01-11	188	10
244	PROJECT	2026-05-11	2026-02-11	188	4
245	PROJECT	2026-03-11	2025-09-11	189	7
246	PROJECT	2026-03-11	2025-10-11	190	11
247	PROJECT	2026-06-11	2026-01-11	190	6
248	PROJECT	2026-02-11	2026-02-11	191	15
249	PROJECT	2026-07-11	2025-12-11	191	1
250	PROJECT	2026-04-11	2025-10-11	192	16
251	PROJECT	2026-05-11	2025-11-11	192	15
252	PROJECT	2026-06-11	2025-10-11	193	7
253	PROJECT	2026-07-11	2025-11-11	194	9
254	PROJECT	2026-02-11	2025-12-11	195	2
255	PROJECT	2026-06-11	2025-11-11	195	1
256	PROJECT	2026-05-11	2026-01-11	196	14
257	PROJECT	2026-04-11	2025-12-11	197	2
258	PROJECT	2026-02-11	2026-01-11	197	10
259	PROJECT	2026-07-11	2026-01-11	198	6
260	PROJECT	2026-05-11	2026-01-11	199	2
261	PROJECT	2026-04-11	2026-01-11	199	15
262	PROJECT	2026-03-11	2025-11-11	200	5
263	PROJECT	2026-07-11	2026-01-11	200	6
264	PROJECT	2026-02-11	2025-10-11	201	2
265	PROJECT	2026-06-11	2026-01-11	202	1
266	PROJECT	2026-07-11	2026-02-11	203	15
267	PROJECT	2026-06-11	2025-09-11	204	3
268	PROJECT	2026-07-11	2026-01-11	204	8
269	PROJECT	2026-07-11	2025-11-11	205	4
270	PROJECT	2026-07-11	2026-02-11	206	12
271	PROJECT	2026-06-11	2025-09-11	206	15
272	PROJECT	2026-02-11	2025-12-11	207	14
273	PROJECT	2026-06-11	2026-02-11	207	8
274	PROJECT	2026-06-11	2025-09-11	208	6
275	PROJECT	2026-04-11	2026-02-11	208	14
276	PROJECT	2026-02-11	2025-10-11	209	16
277	PROJECT	2026-04-11	2026-02-11	210	12
278	PROJECT	2026-04-11	2025-09-11	211	6
\.


--
-- Data for Name: employees; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.employees (id, cost_center, email, gender, grade, hire_date, hiring_type, job_level, legal_entity, location, name, nationality, oracle_id, reason_of_leave, resignation_date, title, manager_id, tower) FROM stdin;
1	\N	ahmed.el-sayed130@company.com	MALE	N1	2022-09-12	FULL_TIME	EXECUTIVE_LEVEL	GS	Egypt	Ahmed El-Sayed	Egyptian	1000	\N	\N	Chief Technology Officer	\N	\N
2	\N	mohamed.hassan884@company.com	MALE	N2	2021-12-09	FULL_TIME	ADVANCED_MANAGER_LEVEL	GS	Egypt	Mohamed Hassan	Egyptian	1001	\N	\N	VP of Infrastructure	1	2
3	\N	sara.ibrahim505@company.com	FEMALE	N2	2021-12-15	FULL_TIME	ADVANCED_MANAGER_LEVEL	GS	UAE	Sara Ibrahim	Egyptian	1002	\N	\N	VP of Applications	1	5
4	\N	khaled.nasser93@company.com	MALE	N2	2022-01-01	FULL_TIME	ADVANCED_MANAGER_LEVEL	GS	UAE	Khaled Nasser	Egyptian	1003	\N	\N	VP of Data & Agility	1	9
5	\N	hassan.farouk276@company.com	MALE	N2	2024-10-23	FULL_TIME	ADVANCED_MANAGER_LEVEL	GS	Egypt	Hassan Farouk	Egyptian	1004	\N	\N	VP of Operations Technology	1	13
6	\N	omar.mohamed32@company.com	MALE	N3	2025-08-25	FULL_TIME	ADVANCED_MANAGER_LEVEL	GS	Egypt	Omar Mohamed	Egyptian	1005	\N	\N	Cloud Services Manager	2	2
7	\N	ali.mahmoud743@company.com	MALE	N3	2022-04-13	FULL_TIME	ADVANCED_MANAGER_LEVEL	GS	UAE	Ali Mahmoud	Egyptian	1006	\N	\N	Infrastructure Manager	2	2
8	\N	marwa.ali863@company.com	FEMALE	N3	2023-08-13	FULL_TIME	ADVANCED_MANAGER_LEVEL	GS	UAE	Marwa Ali	Egyptian	1007	\N	\N	Development Manager	3	5
9	\N	dina.salem243@company.com	FEMALE	N3	2026-01-12	FULL_TIME	ADVANCED_MANAGER_LEVEL	GS	Egypt	Dina Salem	Egyptian	1008	\N	\N	QA Manager	3	5
10	\N	youssef.kamal458@company.com	MALE	N3	2025-06-10	FULL_TIME	ADVANCED_MANAGER_LEVEL	GS	KSA	Youssef Kamal	Egyptian	1009	\N	\N	Agility Manager	4	9
11	\N	reem.rashad730@company.com	FEMALE	N3	2024-12-08	FULL_TIME	ADVANCED_MANAGER_LEVEL	GS	Egypt	Reem Rashad	Egyptian	1010	\N	\N	Scrum Manager	4	9
12	\N	mahmoud.ibrahim85@company.com	MALE	N3	2021-05-21	FULL_TIME	ADVANCED_MANAGER_LEVEL	GS	UAE	Mahmoud Ibrahim	Egyptian	1011	\N	\N	Automation Manager	5	13
13	\N	layla.hassan212@company.com	FEMALE	N3	2024-02-29	FULL_TIME	ADVANCED_MANAGER_LEVEL	GS	KSA	Layla Hassan	Egyptian	1012	\N	\N	Control Systems Manager	5	13
14	\N	nour.mostafa164@company.com	FEMALE	N4	2025-12-08	FULL_TIME	MID_LEVEL	GS	UAE	Nour Mostafa	Egyptian	1013	\N	\N	Cloud Team Lead	6	2
15	\N	fatma.ahmed823@company.com	FEMALE	N4	2020-09-15	FULL_TIME	MID_LEVEL	GS	KSA	Fatma Ahmed	Egyptian	1014	\N	\N	Security Team Lead	7	2
16	\N	hana.mohamed534@company.com	FEMALE	N4	2023-07-22	FULL_TIME	MID_LEVEL	GS	KSA	Hana Mohamed	Egyptian	1015	\N	\N	Frontend Team Lead	8	5
17	\N	aisha.ali157@company.com	FEMALE	N4	2022-11-06	FULL_TIME	MID_LEVEL	GS	KSA	Aisha Ali	Egyptian	1016	\N	\N	Testing Team Lead	9	5
18	\N	mona.hassan440@company.com	FEMALE	N4	2024-01-17	FULL_TIME	MID_LEVEL	GS	UAE	Mona Hassan	Egyptian	1017	\N	\N	Scrum Master	10	9
19	\N	ahmed.abdel160@company.com	MALE	N4	2021-06-04	FULL_TIME	MID_LEVEL	GS	KSA	Ahmed Abdel	Egyptian	1018	\N	\N	Data Engineering Lead	11	9
20	\N	mohamed.kamal59@company.com	MALE	N4	2026-02-08	FULL_TIME	MID_LEVEL	GS	Egypt	Mohamed Kamal	Egyptian	1019	\N	\N	PLC Team Lead	12	13
21	\N	omar.salem897@company.com	MALE	N4	2021-11-12	FULL_TIME	MID_LEVEL	GS	UAE	Omar Salem	Egyptian	1020	\N	\N	SCADA Team Lead	13	13
22	\N	layla.mostafa798@company.com	FEMALE	5	2021-04-03	FULL_TIME	ENTRY_LEVEL	GS	KSA	Layla Mostafa	Egyptian	1021	\N	\N	Engineer	6	2
23	\N	marwa.rashad453@company.com	FEMALE	C	2021-10-22	FULL_TIME	ENTRY_LEVEL	GS	KSA	Marwa Rashad	Egyptian	1022	\N	\N	Engineer	7	2
24	\N	mahmoud.nasser625@company.com	MALE	7	2025-07-26	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Mahmoud Nasser	Egyptian	1023	\N	\N	Engineer	8	5
25	\N	nour.kamal807@company.com	FEMALE	5	2025-10-25	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Nour Kamal	Egyptian	1024	\N	\N	Engineer	9	5
26	\N	aisha.ahmed747@company.com	FEMALE	4	2020-11-06	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Aisha Ahmed	Egyptian	1025	\N	\N	Engineer	10	9
27	\N	marwa.mohamed229@company.com	FEMALE	6	2022-01-27	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Marwa Mohamed	Egyptian	1026	\N	\N	Engineer	11	9
28	\N	hassan.ali532@company.com	MALE	7	2023-06-04	FULL_TIME	ENTRY_LEVEL	GS	KSA	Hassan Ali	Egyptian	1027	\N	\N	Engineer	12	13
29	\N	layla.kamal275@company.com	FEMALE	5	2023-10-19	FULL_TIME	ENTRY_LEVEL	GS	UAE	Layla Kamal	Egyptian	1028	\N	\N	Engineer	13	13
30	\N	nour.hassan31@company.com	FEMALE	4	2023-11-28	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Nour Hassan	Egyptian	1029	\N	\N	Engineer	14	2
43	\N	dina.rashad866@company.com	FEMALE	6	2021-04-12	FULL_TIME	ENTRY_LEVEL	GS	KSA	Dina Rashad	Egyptian	1042	\N	\N	Engineer	11	9
44	\N	fatma.mahmoud793@company.com	FEMALE	C	2025-07-17	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Fatma Mahmoud	Egyptian	1043	\N	\N	Engineer	12	13
45	\N	sara.ahmed269@company.com	FEMALE	5	2025-10-12	FULL_TIME	ENTRY_LEVEL	GS	KSA	Sara Ahmed	Egyptian	1044	\N	\N	Engineer	13	13
46	\N	khaled.rashad150@company.com	MALE	5	2024-10-11	FULL_TIME	ENTRY_LEVEL	GS	KSA	Khaled Rashad	Egyptian	1045	\N	\N	Engineer	14	2
47	\N	hassan.ibrahim359@company.com	MALE	4	2022-11-16	FULL_TIME	ENTRY_LEVEL	GS	UAE	Hassan Ibrahim	Egyptian	1046	\N	\N	Engineer	15	2
48	\N	layla.mahmoud511@company.com	FEMALE	C	2024-04-09	FULL_TIME	ENTRY_LEVEL	GS	UAE	Layla Mahmoud	Egyptian	1047	\N	\N	Engineer	16	5
49	\N	mona.nasser174@company.com	FEMALE	4	2025-01-02	FULL_TIME	ENTRY_LEVEL	GS	KSA	Mona Nasser	Egyptian	1048	\N	\N	Engineer	17	5
50	\N	layla.nasser266@company.com	FEMALE	6	2021-10-08	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Layla Nasser	Egyptian	1049	\N	\N	Engineer	18	9
32	\N	layla.ahmed322@company.com	FEMALE	3	2024-12-05	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Layla Ahmed	Egyptian	1031	Voluntary resignation	2025-11-23	Engineer	16	5
33	\N	nour.mostafa238@company.com	FEMALE	5	2024-04-10	FULL_TIME	ENTRY_LEVEL	GS	KSA	Nour Mostafa	Egyptian	1032	Voluntary resignation	2025-11-23	Engineer	17	5
35	\N	mahmoud.farouk904@company.com	MALE	C	2023-12-26	FULL_TIME	ENTRY_LEVEL	GS	UAE	Mahmoud Farouk	Egyptian	1034	Voluntary resignation	2025-11-24	Engineer	19	9
36	\N	mona.mohamed630@company.com	FEMALE	C	2023-04-27	FULL_TIME	ENTRY_LEVEL	GS	UAE	Mona Mohamed	Egyptian	1035	Voluntary resignation	2025-11-24	Engineer	20	13
37	\N	ali.farouk600@company.com	MALE	4	2025-03-06	FULL_TIME	ENTRY_LEVEL	GS	UAE	Ali Farouk	Egyptian	1036	Voluntary resignation	2025-11-24	Engineer	21	13
38	\N	ali.mostafa740@company.com	MALE	3	2025-12-08	FULL_TIME	ENTRY_LEVEL	GS	UAE	Ali Mostafa	Egyptian	1037	Voluntary resignation	2025-11-24	Engineer	6	2
39	\N	youssef.abdel29@company.com	MALE	6	2025-03-24	FULL_TIME	ENTRY_LEVEL	GS	KSA	Youssef Abdel	Egyptian	1038	Voluntary resignation	2025-11-24	Engineer	7	2
41	\N	aisha.salem46@company.com	FEMALE	7	2025-07-16	FULL_TIME	ENTRY_LEVEL	GS	KSA	Aisha Salem	Egyptian	1040	Voluntary resignation	2025-11-24	Engineer	9	5
42	\N	khaled.mostafa536@company.com	MALE	C	2024-10-07	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Khaled Mostafa	Egyptian	1041	Voluntary resignation	2025-11-24	Engineer	10	9
51	\N	youssef.el-sayed386@company.com	MALE	3	2024-12-07	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Youssef El-Sayed	Egyptian	1050	\N	\N	Engineer	19	9
52	\N	nour.abdel141@company.com	FEMALE	6	2021-05-18	FULL_TIME	ENTRY_LEVEL	GS	UAE	Nour Abdel	Egyptian	1051	\N	\N	Engineer	20	13
53	\N	layla.kamal881@company.com	FEMALE	6	2025-06-03	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Layla Kamal	Egyptian	1052	\N	\N	Engineer	21	13
54	\N	ali.hassan815@company.com	MALE	6	2022-05-25	FULL_TIME	ENTRY_LEVEL	GS	KSA	Ali Hassan	Egyptian	1053	\N	\N	Engineer	6	2
55	\N	marwa.nasser64@company.com	FEMALE	7	2023-03-14	FULL_TIME	ENTRY_LEVEL	GS	KSA	Marwa Nasser	Egyptian	1054	\N	\N	Engineer	7	2
56	\N	fatma.salem294@company.com	FEMALE	3	2023-12-16	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Fatma Salem	Egyptian	1055	\N	\N	Engineer	8	5
57	\N	mohamed.ali186@company.com	MALE	6	2021-08-13	FULL_TIME	ENTRY_LEVEL	GS	KSA	Mohamed Ali	Egyptian	1056	\N	\N	Engineer	9	5
58	\N	reem.mahmoud452@company.com	FEMALE	C	2024-12-28	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Reem Mahmoud	Egyptian	1057	\N	\N	Engineer	10	9
59	\N	youssef.salem725@company.com	MALE	5	2022-03-04	FULL_TIME	ENTRY_LEVEL	GS	UAE	Youssef Salem	Egyptian	1058	\N	\N	Engineer	11	9
60	\N	ahmed.mohamed507@company.com	MALE	5	2020-09-27	FULL_TIME	ENTRY_LEVEL	GS	UAE	Ahmed Mohamed	Egyptian	1059	\N	\N	Engineer	12	13
62	\N	aisha.ibrahim913@company.com	FEMALE	6	2021-01-02	FULL_TIME	ENTRY_LEVEL	GS	KSA	Aisha Ibrahim	Egyptian	1061	\N	\N	Engineer	14	2
66	\N	mahmoud.ibrahim248@company.com	MALE	3	2024-02-01	FULL_TIME	ENTRY_LEVEL	GS	UAE	Mahmoud Ibrahim	Egyptian	1065	\N	\N	Engineer	18	9
73	\N	hana.hassan718@company.com	FEMALE	6	2023-10-13	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Hana Hassan	Egyptian	1072	\N	\N	Engineer	9	5
76	\N	mohamed.mahmoud432@company.com	MALE	6	2023-10-17	FULL_TIME	ENTRY_LEVEL	GS	KSA	Mohamed Mahmoud	Egyptian	1075	\N	\N	Engineer	12	13
77	\N	mahmoud.mohamed717@company.com	MALE	5	2023-02-15	FULL_TIME	ENTRY_LEVEL	GS	UAE	Mahmoud Mohamed	Egyptian	1076	\N	\N	Engineer	13	13
78	\N	hassan.salem347@company.com	MALE	4	2020-09-12	FULL_TIME	ENTRY_LEVEL	GS	UAE	Hassan Salem	Egyptian	1077	\N	\N	Engineer	14	2
79	\N	mona.mostafa510@company.com	FEMALE	C	2024-02-22	FULL_TIME	ENTRY_LEVEL	GS	KSA	Mona Mostafa	Egyptian	1078	\N	\N	Engineer	15	2
80	\N	dina.mahmoud208@company.com	FEMALE	3	2025-10-26	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Dina Mahmoud	Egyptian	1079	\N	\N	Engineer	16	5
81	\N	mahmoud.kamal249@company.com	MALE	5	2022-11-06	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Mahmoud Kamal	Egyptian	1080	\N	\N	Engineer	17	5
82	\N	ali.mostafa447@company.com	MALE	4	2023-01-04	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Ali Mostafa	Egyptian	1081	\N	\N	Engineer	18	9
83	\N	marwa.ahmed711@company.com	FEMALE	4	2023-12-18	FULL_TIME	ENTRY_LEVEL	GS	KSA	Marwa Ahmed	Egyptian	1082	\N	\N	Engineer	19	9
84	\N	ali.el-sayed904@company.com	MALE	C	2025-12-19	FULL_TIME	ENTRY_LEVEL	GS	UAE	Ali El-Sayed	Egyptian	1083	\N	\N	Engineer	20	13
85	\N	hana.hassan894@company.com	FEMALE	C	2024-04-30	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Hana Hassan	Egyptian	1084	\N	\N	Engineer	21	13
86	\N	mohamed.hassan230@company.com	MALE	3	2025-02-16	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Mohamed Hassan	Egyptian	1085	\N	\N	Engineer	6	2
87	\N	sara.mahmoud864@company.com	FEMALE	4	2021-08-18	FULL_TIME	ENTRY_LEVEL	GS	KSA	Sara Mahmoud	Egyptian	1086	\N	\N	Engineer	7	2
88	\N	mohamed.mohamed559@company.com	MALE	6	2024-06-13	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Mohamed Mohamed	Egyptian	1087	\N	\N	Engineer	8	5
89	\N	dina.mahmoud599@company.com	FEMALE	5	2025-03-04	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Dina Mahmoud	Egyptian	1088	\N	\N	Engineer	9	5
90	\N	layla.el-sayed806@company.com	FEMALE	3	2022-01-23	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Layla El-Sayed	Egyptian	1089	\N	\N	Engineer	10	9
92	\N	sara.ali786@company.com	FEMALE	6	2024-02-26	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Sara Ali	Egyptian	1091	\N	\N	Engineer	12	13
93	\N	layla.ali234@company.com	FEMALE	C	2024-02-29	FULL_TIME	ENTRY_LEVEL	GS	KSA	Layla Ali	Egyptian	1092	\N	\N	Engineer	13	13
95	\N	nour.el-sayed150@company.com	FEMALE	C	2023-12-26	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Nour El-Sayed	Egyptian	1094	\N	\N	Engineer	15	2
97	\N	nour.salem685@company.com	FEMALE	C	2023-02-19	FULL_TIME	ENTRY_LEVEL	GS	KSA	Nour Salem	Egyptian	1096	\N	\N	Engineer	17	5
99	\N	hana.mahmoud338@company.com	FEMALE	3	2024-04-18	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Hana Mahmoud	Egyptian	1098	\N	\N	Engineer	19	9
100	\N	ahmed.rashad726@company.com	MALE	7	2024-07-01	FULL_TIME	ENTRY_LEVEL	GS	UAE	Ahmed Rashad	Egyptian	1099	\N	\N	Engineer	20	13
91	\N	marwa.el-sayed757@company.com	FEMALE	7	2024-11-03	FULL_TIME	ENTRY_LEVEL	GS	UAE	Marwa El-Sayed	Egyptian	1090	Voluntary resignation	2025-12-26	Engineer	11	9
69	\N	sara.ali959@company.com	FEMALE	6	2025-07-02	FULL_TIME	ENTRY_LEVEL	GS	UAE	Sara Ali	Egyptian	1068	Voluntary resignation	2026-02-09	Engineer	21	13
64	\N	hana.mohamed605@company.com	FEMALE	4	2022-04-26	FULL_TIME	ENTRY_LEVEL	GS	UAE	Hana Mohamed	Egyptian	1063	Voluntary resignation	2026-02-08	Engineer	16	5
70	\N	khaled.salem860@company.com	MALE	3	2024-03-07	FULL_TIME	ENTRY_LEVEL	GS	KSA	Khaled Salem	Egyptian	1069	Voluntary resignation	2026-02-09	Engineer	6	2
71	\N	omar.hassan923@company.com	MALE	7	2021-07-07	FULL_TIME	ENTRY_LEVEL	GS	UAE	Omar Hassan	Egyptian	1070	Voluntary resignation	2026-02-09	Engineer	7	2
72	\N	nour.ahmed765@company.com	FEMALE	3	2024-06-25	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Nour Ahmed	Egyptian	1071	Voluntary resignation	2026-02-09	Engineer	8	5
74	\N	hassan.mohamed165@company.com	MALE	5	2021-03-07	FULL_TIME	ENTRY_LEVEL	GS	KSA	Hassan Mohamed	Egyptian	1073	Voluntary resignation	2026-02-09	Engineer	10	9
101	\N	marwa.farouk100@company.com	FEMALE	6	2025-10-25	FULL_TIME	ENTRY_LEVEL	GS	KSA	Marwa Farouk	Egyptian	1100	Voluntary resignation	2026-01-25	Engineer	21	13
63	\N	omar.salem984@company.com	MALE	5	2022-09-17	FULL_TIME	ENTRY_LEVEL	GS	UAE	Omar Salem	Egyptian	1062	Voluntary resignation	2026-01-25	Engineer	15	2
96	\N	ali.ahmed819@company.com	MALE	7	2025-08-21	FULL_TIME	ENTRY_LEVEL	GS	KSA	Ali Ahmed	Egyptian	1095	Voluntary resignation	2026-01-25	Engineer	16	5
65	\N	layla.abdel688@company.com	FEMALE	4	2024-05-23	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Layla Abdel	Egyptian	1064	Voluntary resignation	2026-01-25	Engineer	17	5
98	\N	layla.mostafa725@company.com	FEMALE	7	2024-02-11	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Layla Mostafa	Egyptian	1097	Voluntary resignation	2026-01-25	Engineer	18	9
75	\N	sara.rashad802@company.com	FEMALE	4	2023-05-26	FULL_TIME	ENTRY_LEVEL	GS	UAE	Sara Rashad	Egyptian	1074	Voluntary resignation	2026-02-09	Engineer	11	9
102	\N	omar.rashad674@company.com	MALE	3	2024-01-25	FULL_TIME	ENTRY_LEVEL	GS	KSA	Omar Rashad	Egyptian	1101	\N	\N	Engineer	6	2
103	\N	marwa.mostafa813@company.com	FEMALE	5	2024-09-11	FULL_TIME	ENTRY_LEVEL	GS	KSA	Marwa Mostafa	Egyptian	1102	\N	\N	Engineer	7	2
104	\N	omar.ali391@company.com	MALE	4	2020-11-21	FULL_TIME	ENTRY_LEVEL	GS	KSA	Omar Ali	Egyptian	1103	\N	\N	Engineer	8	5
105	\N	dina.hassan337@company.com	FEMALE	7	2021-10-07	FULL_TIME	ENTRY_LEVEL	GS	UAE	Dina Hassan	Egyptian	1104	\N	\N	Engineer	9	5
106	\N	dina.mahmoud564@company.com	FEMALE	5	2026-01-27	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Dina Mahmoud	Egyptian	1105	\N	\N	Engineer	10	9
107	\N	mahmoud.abdel514@company.com	MALE	4	2023-10-02	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Mahmoud Abdel	Egyptian	1106	\N	\N	Engineer	11	9
108	\N	reem.ali350@company.com	FEMALE	5	2023-12-10	FULL_TIME	ENTRY_LEVEL	GS	UAE	Reem Ali	Egyptian	1107	\N	\N	Engineer	12	13
109	\N	omar.rashad26@company.com	MALE	4	2023-12-27	FULL_TIME	ENTRY_LEVEL	GS	KSA	Omar Rashad	Egyptian	1108	\N	\N	Engineer	13	13
110	\N	layla.mohamed572@company.com	FEMALE	C	2022-08-29	FULL_TIME	ENTRY_LEVEL	GS	UAE	Layla Mohamed	Egyptian	1109	\N	\N	Engineer	14	2
111	\N	ali.ibrahim946@company.com	MALE	6	2023-04-05	FULL_TIME	ENTRY_LEVEL	GS	KSA	Ali Ibrahim	Egyptian	1110	\N	\N	Engineer	15	2
112	\N	mona.mohamed435@company.com	FEMALE	C	2023-12-06	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Mona Mohamed	Egyptian	1111	\N	\N	Engineer	16	5
113	\N	youssef.ali503@company.com	MALE	C	2025-02-03	FULL_TIME	ENTRY_LEVEL	GS	UAE	Youssef Ali	Egyptian	1112	\N	\N	Engineer	17	5
114	\N	dina.kamal973@company.com	FEMALE	7	2025-07-27	FULL_TIME	ENTRY_LEVEL	GS	UAE	Dina Kamal	Egyptian	1113	\N	\N	Engineer	18	9
115	\N	hassan.ali317@company.com	MALE	7	2021-11-11	FULL_TIME	ENTRY_LEVEL	GS	UAE	Hassan Ali	Egyptian	1114	\N	\N	Engineer	19	9
116	\N	youssef.mahmoud451@company.com	MALE	C	2025-01-08	FULL_TIME	ENTRY_LEVEL	GS	KSA	Youssef Mahmoud	Egyptian	1115	\N	\N	Engineer	20	13
117	\N	hassan.ali794@company.com	MALE	C	2021-04-19	FULL_TIME	ENTRY_LEVEL	GS	KSA	Hassan Ali	Egyptian	1116	\N	\N	Engineer	21	13
118	\N	ahmed.ali690@company.com	MALE	C	2023-08-19	FULL_TIME	ENTRY_LEVEL	GS	KSA	Ahmed Ali	Egyptian	1117	\N	\N	Engineer	6	2
119	\N	layla.farouk788@company.com	FEMALE	6	2022-05-11	FULL_TIME	ENTRY_LEVEL	GS	KSA	Layla Farouk	Egyptian	1118	\N	\N	Engineer	7	2
120	\N	aisha.nasser818@company.com	FEMALE	5	2023-02-23	FULL_TIME	ENTRY_LEVEL	GS	UAE	Aisha Nasser	Egyptian	1119	\N	\N	Engineer	8	5
122	\N	mona.rashad370@company.com	FEMALE	C	2025-08-08	FULL_TIME	ENTRY_LEVEL	GS	KSA	Mona Rashad	Egyptian	1121	\N	\N	Engineer	10	9
123	\N	reem.hassan190@company.com	FEMALE	C	2023-01-27	FULL_TIME	ENTRY_LEVEL	GS	UAE	Reem Hassan	Egyptian	1122	\N	\N	Engineer	11	9
125	\N	khaled.mohamed186@company.com	MALE	4	2025-08-03	FULL_TIME	ENTRY_LEVEL	GS	KSA	Khaled Mohamed	Egyptian	1124	\N	\N	Engineer	13	13
128	\N	aisha.el-sayed732@company.com	FEMALE	C	2022-02-23	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Aisha El-Sayed	Egyptian	1127	\N	\N	Engineer	16	5
131	\N	nour.mahmoud795@company.com	FEMALE	C	2024-11-26	FULL_TIME	ENTRY_LEVEL	GS	KSA	Nour Mahmoud	Egyptian	1130	\N	\N	Engineer	19	9
136	\N	khaled.ahmed596@company.com	MALE	6	2022-12-13	FULL_TIME	ENTRY_LEVEL	GS	UAE	Khaled Ahmed	Egyptian	1135	\N	\N	Engineer	8	5
137	\N	reem.mahmoud349@company.com	FEMALE	C	2021-11-25	FULL_TIME	ENTRY_LEVEL	GS	UAE	Reem Mahmoud	Egyptian	1136	\N	\N	Engineer	9	5
140	\N	layla.mostafa67@company.com	FEMALE	4	2023-07-28	FULL_TIME	ENTRY_LEVEL	GS	KSA	Layla Mostafa	Egyptian	1139	\N	\N	Engineer	12	13
141	\N	aisha.kamal685@company.com	FEMALE	6	2021-01-19	FULL_TIME	ENTRY_LEVEL	GS	KSA	Aisha Kamal	Egyptian	1140	\N	\N	Engineer	13	13
142	\N	hana.rashad299@company.com	FEMALE	C	2024-07-11	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Hana Rashad	Egyptian	1141	\N	\N	Engineer	14	2
143	\N	fatma.ali362@company.com	FEMALE	3	2024-01-30	FULL_TIME	ENTRY_LEVEL	GS	KSA	Fatma Ali	Egyptian	1142	\N	\N	Engineer	15	2
144	\N	omar.salem730@company.com	MALE	7	2024-09-24	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Omar Salem	Egyptian	1143	\N	\N	Engineer	16	5
145	\N	mohamed.mahmoud391@company.com	MALE	7	2022-11-08	FULL_TIME	ENTRY_LEVEL	GS	KSA	Mohamed Mahmoud	Egyptian	1144	\N	\N	Engineer	17	5
146	\N	hana.nasser660@company.com	FEMALE	C	2021-12-31	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Hana Nasser	Egyptian	1145	\N	\N	Engineer	18	9
147	\N	sara.ahmed682@company.com	FEMALE	6	2025-07-20	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Sara Ahmed	Egyptian	1146	\N	\N	Engineer	19	9
148	\N	hana.farouk501@company.com	FEMALE	C	2024-08-05	FULL_TIME	ENTRY_LEVEL	GS	UAE	Hana Farouk	Egyptian	1147	\N	\N	Engineer	20	13
149	\N	mohamed.el-sayed948@company.com	MALE	C	2024-06-24	FULL_TIME	ENTRY_LEVEL	GS	KSA	Mohamed El-Sayed	Egyptian	1148	\N	\N	Engineer	21	13
150	\N	nour.abdel859@company.com	FEMALE	4	2022-09-19	FULL_TIME	ENTRY_LEVEL	GS	KSA	Nour Abdel	Egyptian	1149	\N	\N	Engineer	6	2
152	\N	mahmoud.salem480@company.com	MALE	7	2023-08-22	FULL_TIME	ENTRY_LEVEL	GS	UAE	Mahmoud Salem	Egyptian	1151	\N	\N	Engineer	8	5
151	\N	hana.mahmoud500@company.com	FEMALE	C	2025-11-12	FULL_TIME	ENTRY_LEVEL	GS	KSA	Hana Mahmoud	Egyptian	1150	Voluntary resignation	2025-11-25	Engineer	7	2
124	\N	hassan.nasser423@company.com	MALE	5	2021-04-10	FULL_TIME	ENTRY_LEVEL	GS	KSA	Hassan Nasser	Egyptian	1123	Voluntary resignation	2025-12-29	Engineer	12	13
126	\N	hassan.el-sayed828@company.com	MALE	4	2023-05-31	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Hassan El-Sayed	Egyptian	1125	Voluntary resignation	2025-12-29	Engineer	14	2
129	\N	nour.el-sayed556@company.com	FEMALE	3	2021-01-09	FULL_TIME	ENTRY_LEVEL	GS	KSA	Nour El-Sayed	Egyptian	1128	Voluntary resignation	2025-12-30	Engineer	17	5
130	\N	sara.hassan107@company.com	FEMALE	6	2022-02-01	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Sara Hassan	Egyptian	1129	Voluntary resignation	2025-12-30	Engineer	18	9
132	\N	mohamed.mahmoud508@company.com	MALE	3	2023-06-07	FULL_TIME	ENTRY_LEVEL	GS	KSA	Mohamed Mahmoud	Egyptian	1131	Voluntary resignation	2025-12-30	Engineer	20	13
133	\N	youssef.abdel471@company.com	MALE	C	2021-03-19	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Youssef Abdel	Egyptian	1132	Voluntary resignation	2025-12-30	Engineer	21	13
134	\N	hassan.ahmed628@company.com	MALE	4	2023-01-01	FULL_TIME	ENTRY_LEVEL	GS	UAE	Hassan Ahmed	Egyptian	1133	Voluntary resignation	2025-12-30	Engineer	6	2
138	\N	ali.abdel212@company.com	MALE	C	2023-07-04	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Ali Abdel	Egyptian	1137	Voluntary resignation	2025-12-30	Engineer	10	9
139	\N	hassan.abdel339@company.com	MALE	7	2021-07-03	FULL_TIME	ENTRY_LEVEL	GS	UAE	Hassan Abdel	Egyptian	1138	Voluntary resignation	2025-12-30	Engineer	11	9
153	\N	omar.ali552@company.com	MALE	5	2022-04-22	FULL_TIME	ENTRY_LEVEL	GS	UAE	Omar Ali	Egyptian	1152	\N	\N	Engineer	9	5
155	\N	layla.mostafa646@company.com	FEMALE	3	2023-10-15	FULL_TIME	ENTRY_LEVEL	GS	KSA	Layla Mostafa	Egyptian	1154	\N	\N	Engineer	11	9
158	\N	hana.farouk85@company.com	FEMALE	6	2022-07-04	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Hana Farouk	Egyptian	1157	\N	\N	Engineer	14	2
159	\N	mohamed.ibrahim606@company.com	MALE	4	2021-05-26	FULL_TIME	ENTRY_LEVEL	GS	UAE	Mohamed Ibrahim	Egyptian	1158	\N	\N	Engineer	15	2
162	\N	marwa.abdel555@company.com	FEMALE	C	2023-11-01	FULL_TIME	ENTRY_LEVEL	GS	KSA	Marwa Abdel	Egyptian	1161	\N	\N	Engineer	18	9
167	\N	reem.farouk901@company.com	FEMALE	5	2022-03-08	FULL_TIME	ENTRY_LEVEL	GS	UAE	Reem Farouk	Egyptian	1166	\N	\N	Engineer	7	2
168	\N	dina.ali940@company.com	FEMALE	7	2022-01-11	FULL_TIME	ENTRY_LEVEL	GS	KSA	Dina Ali	Egyptian	1167	\N	\N	Engineer	8	5
169	\N	hana.farouk60@company.com	FEMALE	3	2024-06-11	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Hana Farouk	Egyptian	1168	\N	\N	Engineer	9	5
172	\N	fatma.rashad775@company.com	FEMALE	6	2023-05-06	FULL_TIME	ENTRY_LEVEL	GS	UAE	Fatma Rashad	Egyptian	1171	\N	\N	Engineer	12	13
173	\N	youssef.mahmoud471@company.com	MALE	4	2021-10-06	FULL_TIME	ENTRY_LEVEL	GS	UAE	Youssef Mahmoud	Egyptian	1172	\N	\N	Engineer	13	13
174	\N	layla.kamal815@company.com	FEMALE	3	2022-08-31	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Layla Kamal	Egyptian	1173	\N	\N	Engineer	14	2
175	\N	hassan.abdel86@company.com	MALE	3	2025-11-27	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Hassan Abdel	Egyptian	1174	\N	\N	Engineer	15	2
176	\N	layla.ibrahim820@company.com	FEMALE	7	2025-07-22	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Layla Ibrahim	Egyptian	1175	\N	\N	Engineer	16	5
177	\N	aisha.farouk361@company.com	FEMALE	6	2024-06-29	FULL_TIME	ENTRY_LEVEL	GS	UAE	Aisha Farouk	Egyptian	1176	\N	\N	Engineer	17	5
178	\N	hassan.ali892@company.com	MALE	7	2024-04-22	FULL_TIME	ENTRY_LEVEL	GS	KSA	Hassan Ali	Egyptian	1177	\N	\N	Engineer	18	9
179	\N	sara.ibrahim983@company.com	FEMALE	4	2023-03-31	FULL_TIME	ENTRY_LEVEL	GS	KSA	Sara Ibrahim	Egyptian	1178	\N	\N	Engineer	19	9
180	\N	mona.hassan718@company.com	FEMALE	5	2021-09-29	FULL_TIME	ENTRY_LEVEL	GS	UAE	Mona Hassan	Egyptian	1179	\N	\N	Engineer	20	13
182	\N	aisha.mostafa257@company.com	FEMALE	4	2022-10-13	FULL_TIME	ENTRY_LEVEL	GS	UAE	Aisha Mostafa	Egyptian	1181	\N	\N	Engineer	6	2
183	\N	sara.el-sayed248@company.com	FEMALE	3	2021-11-08	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Sara El-Sayed	Egyptian	1182	\N	\N	Engineer	7	2
184	\N	reem.ali786@company.com	FEMALE	5	2022-08-23	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Reem Ali	Egyptian	1183	\N	\N	Engineer	8	5
186	\N	mohamed.mohamed692@company.com	MALE	3	2020-10-03	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Mohamed Mohamed	Egyptian	1185	\N	\N	Engineer	10	9
187	\N	omar.mohamed822@company.com	MALE	3	2021-11-09	FULL_TIME	ENTRY_LEVEL	GS	KSA	Omar Mohamed	Egyptian	1186	\N	\N	Engineer	11	9
189	\N	youssef.el-sayed605@company.com	MALE	5	2024-08-18	FULL_TIME	ENTRY_LEVEL	GS	UAE	Youssef El-Sayed	Egyptian	1188	\N	\N	Engineer	13	13
191	\N	layla.abdel344@company.com	FEMALE	C	2025-07-23	FULL_TIME	ENTRY_LEVEL	GS	KSA	Layla Abdel	Egyptian	1190	\N	\N	Engineer	15	2
192	\N	ali.ahmed152@company.com	MALE	C	2023-07-06	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Ali Ahmed	Egyptian	1191	\N	\N	Engineer	16	5
193	\N	sara.el-sayed824@company.com	FEMALE	3	2021-01-30	FULL_TIME	ENTRY_LEVEL	GS	UAE	Sara El-Sayed	Egyptian	1192	\N	\N	Engineer	17	5
195	\N	mona.kamal926@company.com	FEMALE	7	2025-06-26	FULL_TIME	ENTRY_LEVEL	GS	UAE	Mona Kamal	Egyptian	1194	\N	\N	Engineer	19	9
197	\N	mona.kamal487@company.com	FEMALE	3	2022-02-24	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Mona Kamal	Egyptian	1196	\N	\N	Engineer	21	13
198	\N	reem.el-sayed488@company.com	FEMALE	6	2025-11-15	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Reem El-Sayed	Egyptian	1197	\N	\N	Engineer	6	2
200	\N	nour.ibrahim295@company.com	FEMALE	C	2021-05-16	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Nour Ibrahim	Egyptian	1199	\N	\N	Engineer	8	5
202	\N	sara.mahmoud759@company.com	FEMALE	7	2022-05-06	FULL_TIME	ENTRY_LEVEL	GS	KSA	Sara Mahmoud	Egyptian	1201	\N	\N	Engineer	10	9
154	\N	layla.farouk374@company.com	FEMALE	3	2022-09-07	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Layla Farouk	Egyptian	1153	Voluntary resignation	2026-02-11	Engineer	10	9
170	\N	mahmoud.salem469@company.com	MALE	6	2025-05-05	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Mahmoud Salem	Egyptian	1169	Voluntary resignation	2026-02-12	Engineer	10	9
156	\N	youssef.el-sayed980@company.com	MALE	C	2023-07-28	FULL_TIME	ENTRY_LEVEL	GS	KSA	Youssef El-Sayed	Egyptian	1155	Voluntary resignation	2026-02-11	Engineer	12	13
157	\N	hassan.mostafa697@company.com	MALE	3	2025-06-30	FULL_TIME	ENTRY_LEVEL	GS	UAE	Hassan Mostafa	Egyptian	1156	Voluntary resignation	2026-02-11	Engineer	13	13
190	\N	reem.abdel220@company.com	FEMALE	5	2024-09-14	FULL_TIME	ENTRY_LEVEL	GS	UAE	Reem Abdel	Egyptian	1189	Voluntary resignation	2026-01-17	Engineer	14	2
160	\N	nour.kamal392@company.com	FEMALE	4	2022-12-05	FULL_TIME	ENTRY_LEVEL	GS	KSA	Nour Kamal	Egyptian	1159	Voluntary resignation	2026-02-12	Engineer	16	5
194	\N	ali.ahmed564@company.com	MALE	5	2020-12-25	FULL_TIME	ENTRY_LEVEL	GS	KSA	Ali Ahmed	Egyptian	1193	Voluntary resignation	2026-01-18	Engineer	18	9
161	\N	hassan.ali77@company.com	MALE	6	2024-11-23	FULL_TIME	ENTRY_LEVEL	GS	KSA	Hassan Ali	Egyptian	1160	Voluntary resignation	2026-02-12	Engineer	17	5
196	\N	sara.hassan342@company.com	FEMALE	4	2023-12-22	FULL_TIME	ENTRY_LEVEL	GS	KSA	Sara Hassan	Egyptian	1195	Voluntary resignation	2026-01-18	Engineer	20	13
199	\N	hana.el-sayed963@company.com	FEMALE	7	2023-05-29	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Hana El-Sayed	Egyptian	1198	Voluntary resignation	2026-01-18	Engineer	7	2
164	\N	hassan.rashad129@company.com	MALE	3	2022-06-09	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Hassan Rashad	Egyptian	1163	Voluntary resignation	2026-02-12	Engineer	20	13
201	\N	sara.ali948@company.com	FEMALE	C	2021-11-28	FULL_TIME	ENTRY_LEVEL	GS	KSA	Sara Ali	Egyptian	1200	Voluntary resignation	2026-01-18	Engineer	9	5
165	\N	layla.kamal200@company.com	FEMALE	C	2021-12-29	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Layla Kamal	Egyptian	1164	Voluntary resignation	2026-02-12	Engineer	21	13
203	\N	reem.nasser724@company.com	FEMALE	4	2025-01-10	FULL_TIME	ENTRY_LEVEL	GS	KSA	Reem Nasser	Egyptian	1202	Voluntary resignation	2026-01-18	Engineer	11	9
185	\N	youssef.ahmed604@company.com	MALE	4	2023-06-18	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Youssef Ahmed	Egyptian	1184	Voluntary resignation	2026-01-18	Engineer	9	5
171	\N	nour.ali987@company.com	FEMALE	C	2021-10-13	FULL_TIME	ENTRY_LEVEL	GS	KSA	Nour Ali	Egyptian	1170	Voluntary resignation	2026-02-12	Engineer	11	9
204	\N	mohamed.el-sayed754@company.com	MALE	C	2024-01-02	FULL_TIME	ENTRY_LEVEL	GS	UAE	Mohamed El-Sayed	Egyptian	1203	\N	\N	Engineer	12	13
205	\N	mona.ali695@company.com	FEMALE	5	2024-10-24	FULL_TIME	ENTRY_LEVEL	GS	KSA	Mona Ali	Egyptian	1204	\N	\N	Engineer	13	13
206	\N	mahmoud.hassan888@company.com	MALE	4	2021-06-14	FULL_TIME	ENTRY_LEVEL	GS	UAE	Mahmoud Hassan	Egyptian	1205	\N	\N	Engineer	14	2
207	\N	khaled.kamal711@company.com	MALE	4	2021-07-04	FULL_TIME	ENTRY_LEVEL	GS	UAE	Khaled Kamal	Egyptian	1206	\N	\N	Engineer	15	2
208	\N	marwa.mostafa697@company.com	FEMALE	7	2024-01-29	FULL_TIME	ENTRY_LEVEL	GS	UAE	Marwa Mostafa	Egyptian	1207	\N	\N	Engineer	16	5
209	\N	dina.ali198@company.com	FEMALE	7	2025-02-23	FULL_TIME	ENTRY_LEVEL	GS	UAE	Dina Ali	Egyptian	1208	\N	\N	Engineer	17	5
210	\N	hassan.mahmoud470@company.com	MALE	3	2021-07-16	FULL_TIME	ENTRY_LEVEL	GS	KSA	Hassan Mahmoud	Egyptian	1209	\N	\N	Engineer	18	9
31	\N	khaled.rashad246@company.com	MALE	6	2023-06-29	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Khaled Rashad	Egyptian	1030	Voluntary resignation	2026-02-06	Engineer	15	2
61	\N	mohamed.mohamed47@company.com	MALE	4	2023-01-04	FULL_TIME	ENTRY_LEVEL	GS	KSA	Mohamed Mohamed	Egyptian	1060	Voluntary resignation	2025-11-14	Engineer	13	13
121	\N	layla.el-sayed319@company.com	FEMALE	4	2024-07-11	FULL_TIME	ENTRY_LEVEL	GS	KSA	Layla El-Sayed	Egyptian	1120	Voluntary resignation	2025-11-30	Engineer	9	5
181	\N	ahmed.ali708@company.com	MALE	3	2025-09-22	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Ahmed Ali	Egyptian	1180	Voluntary resignation	2026-01-26	Engineer	21	13
94	\N	mohamed.rashad16@company.com	MALE	3	2024-04-09	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Mohamed Rashad	Egyptian	1093	Voluntary resignation	2026-01-24	Engineer	14	2
211	\N	aisha.mostafa237@company.com	FEMALE	4	2023-08-31	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Aisha Mostafa	Egyptian	1210	Voluntary resignation	2026-02-06	Engineer	19	9
34	\N	hana.farouk627@company.com	FEMALE	5	2022-05-10	FULL_TIME	ENTRY_LEVEL	GS	KSA	Hana Farouk	Egyptian	1033	Voluntary resignation	2025-11-23	Engineer	18	9
127	\N	youssef.ibrahim56@company.com	MALE	7	2023-01-25	FULL_TIME	ENTRY_LEVEL	GS	UAE	Youssef Ibrahim	Egyptian	1126	Voluntary resignation	2025-12-29	Engineer	15	2
188	\N	mahmoud.abdel944@company.com	MALE	4	2021-09-03	FULL_TIME	ENTRY_LEVEL	GS	KSA	Mahmoud Abdel	Egyptian	1187	Voluntary resignation	2026-02-07	Engineer	12	13
163	\N	mahmoud.mahmoud957@company.com	MALE	5	2024-01-11	FULL_TIME	ENTRY_LEVEL	GS	KSA	Mahmoud Mahmoud	Egyptian	1162	Voluntary resignation	2026-02-12	Engineer	19	9
68	\N	omar.el-sayed7@company.com	MALE	3	2026-01-31	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Omar El-Sayed	Egyptian	1067	Voluntary resignation	2026-02-07	Engineer	20	13
40	\N	sara.mahmoud6@company.com	FEMALE	3	2025-07-14	FULL_TIME	ENTRY_LEVEL	GS	Egypt	Sara Mahmoud	Egyptian	1039	Voluntary resignation	2025-11-24	Engineer	8	5
67	\N	khaled.rashad857@company.com	MALE	5	2024-01-02	FULL_TIME	ENTRY_LEVEL	GS	KSA	Khaled Rashad	Egyptian	1066	Voluntary resignation	2026-01-25	Engineer	19	9
135	\N	nour.abdel699@company.com	FEMALE	7	2025-01-19	FULL_TIME	ENTRY_LEVEL	GS	UAE	Nour Abdel	Egyptian	1134	Voluntary resignation	2025-12-30	Engineer	7	2
166	\N	ahmed.nasser860@company.com	MALE	7	2021-06-21	FULL_TIME	ENTRY_LEVEL	GS	KSA	Ahmed Nasser	Egyptian	1165	Voluntary resignation	2026-02-07	Engineer	6	2
\.


--
-- Data for Name: employees_skills; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.employees_skills (id, skill_grade, skill_level, employee_id, skill_id) FROM stdin;
1	ADVANCED	PRIMARY	2	7
2	INTERMEDIATE	SECONDARY	2	2
3	ADVANCED	PRIMARY	3	16
4	BEGINNER	PRIMARY	4	37
5	BEGINNER	PRIMARY	5	57
6	ADVANCED	PRIMARY	6	5
7	INTERMEDIATE	PRIMARY	7	3
8	ADVANCED	SECONDARY	7	4
9	ADVANCED	PRIMARY	8	16
10	BEGINNER	SECONDARY	8	18
11	ADVANCED	PRIMARY	9	19
12	BEGINNER	PRIMARY	10	35
13	INTERMEDIATE	SECONDARY	10	37
14	ADVANCED	SECONDARY	10	34
15	BEGINNER	PRIMARY	11	33
16	INTERMEDIATE	SECONDARY	11	36
17	INTERMEDIATE	SECONDARY	11	37
18	ADVANCED	PRIMARY	12	52
19	INTERMEDIATE	SECONDARY	12	56
20	ADVANCED	SECONDARY	12	55
21	ADVANCED	PRIMARY	13	56
22	BEGINNER	SECONDARY	13	52
23	ADVANCED	SECONDARY	13	53
24	ADVANCED	PRIMARY	14	6
25	BEGINNER	PRIMARY	15	6
26	ADVANCED	PRIMARY	16	17
27	BEGINNER	SECONDARY	16	19
28	INTERMEDIATE	PRIMARY	17	19
29	BEGINNER	SECONDARY	17	16
30	ADVANCED	PRIMARY	18	37
31	BEGINNER	SECONDARY	18	33
32	ADVANCED	PRIMARY	19	34
33	ADVANCED	SECONDARY	19	37
34	BEGINNER	PRIMARY	20	53
35	INTERMEDIATE	PRIMARY	21	54
36	ADVANCED	SECONDARY	21	55
37	BEGINNER	PRIMARY	22	3
38	ADVANCED	SECONDARY	22	5
39	ADVANCED	PRIMARY	23	7
40	BEGINNER	SECONDARY	23	1
41	INTERMEDIATE	PRIMARY	24	19
42	BEGINNER	PRIMARY	25	17
43	ADVANCED	PRIMARY	26	34
44	BEGINNER	SECONDARY	26	33
45	ADVANCED	PRIMARY	27	37
46	BEGINNER	SECONDARY	27	35
47	ADVANCED	PRIMARY	28	57
48	BEGINNER	SECONDARY	28	54
49	BEGINNER	SECONDARY	28	56
50	BEGINNER	PRIMARY	29	56
51	ADVANCED	SECONDARY	29	54
52	BEGINNER	SECONDARY	29	57
53	BEGINNER	PRIMARY	30	1
54	BEGINNER	PRIMARY	31	3
55	BEGINNER	PRIMARY	32	15
56	ADVANCED	SECONDARY	32	14
57	ADVANCED	PRIMARY	33	18
58	INTERMEDIATE	SECONDARY	33	15
59	INTERMEDIATE	SECONDARY	33	17
60	ADVANCED	PRIMARY	34	37
61	BEGINNER	SECONDARY	34	38
62	BEGINNER	SECONDARY	34	35
63	ADVANCED	PRIMARY	35	35
64	ADVANCED	PRIMARY	36	55
65	ADVANCED	SECONDARY	36	53
66	BEGINNER	SECONDARY	36	56
67	INTERMEDIATE	PRIMARY	37	52
68	INTERMEDIATE	PRIMARY	38	6
69	BEGINNER	SECONDARY	38	7
70	BEGINNER	SECONDARY	38	4
71	ADVANCED	PRIMARY	39	7
72	BEGINNER	PRIMARY	40	18
73	INTERMEDIATE	PRIMARY	41	18
74	INTERMEDIATE	PRIMARY	42	36
75	ADVANCED	SECONDARY	42	37
76	INTERMEDIATE	PRIMARY	43	36
77	INTERMEDIATE	SECONDARY	43	35
78	INTERMEDIATE	PRIMARY	44	56
79	INTERMEDIATE	SECONDARY	44	54
80	BEGINNER	PRIMARY	45	54
81	BEGINNER	SECONDARY	45	56
82	BEGINNER	PRIMARY	46	6
83	BEGINNER	PRIMARY	47	7
84	ADVANCED	SECONDARY	47	2
85	INTERMEDIATE	SECONDARY	47	3
86	INTERMEDIATE	PRIMARY	48	18
87	BEGINNER	PRIMARY	49	18
88	BEGINNER	SECONDARY	49	15
89	INTERMEDIATE	SECONDARY	49	17
90	BEGINNER	PRIMARY	50	38
91	INTERMEDIATE	PRIMARY	51	38
92	BEGINNER	SECONDARY	51	33
93	BEGINNER	SECONDARY	51	36
94	BEGINNER	PRIMARY	52	55
95	ADVANCED	SECONDARY	52	53
96	ADVANCED	SECONDARY	52	57
97	BEGINNER	PRIMARY	53	55
98	BEGINNER	SECONDARY	53	56
99	BEGINNER	PRIMARY	54	3
100	INTERMEDIATE	SECONDARY	54	5
101	INTERMEDIATE	PRIMARY	55	5
102	BEGINNER	SECONDARY	55	2
103	INTERMEDIATE	PRIMARY	56	15
104	INTERMEDIATE	SECONDARY	56	17
105	BEGINNER	SECONDARY	56	14
106	INTERMEDIATE	PRIMARY	57	14
107	INTERMEDIATE	PRIMARY	58	38
108	BEGINNER	SECONDARY	58	33
109	BEGINNER	PRIMARY	59	34
110	BEGINNER	SECONDARY	59	33
111	INTERMEDIATE	PRIMARY	60	56
112	INTERMEDIATE	PRIMARY	61	55
113	BEGINNER	PRIMARY	62	7
114	BEGINNER	SECONDARY	62	1
115	ADVANCED	PRIMARY	63	7
116	INTERMEDIATE	SECONDARY	63	5
117	ADVANCED	PRIMARY	64	14
118	INTERMEDIATE	PRIMARY	65	16
119	ADVANCED	SECONDARY	65	15
120	ADVANCED	SECONDARY	65	18
121	ADVANCED	PRIMARY	66	36
122	ADVANCED	SECONDARY	66	35
123	BEGINNER	PRIMARY	67	36
124	INTERMEDIATE	SECONDARY	67	35
125	BEGINNER	PRIMARY	68	56
126	ADVANCED	SECONDARY	68	54
127	INTERMEDIATE	SECONDARY	68	57
128	ADVANCED	PRIMARY	69	52
129	ADVANCED	SECONDARY	69	55
130	BEGINNER	SECONDARY	69	57
131	INTERMEDIATE	PRIMARY	70	5
132	BEGINNER	PRIMARY	71	6
133	INTERMEDIATE	PRIMARY	72	17
134	ADVANCED	PRIMARY	73	15
135	BEGINNER	PRIMARY	74	38
136	INTERMEDIATE	SECONDARY	74	36
137	BEGINNER	SECONDARY	74	35
138	ADVANCED	PRIMARY	75	35
139	BEGINNER	SECONDARY	75	36
140	BEGINNER	SECONDARY	75	34
141	INTERMEDIATE	PRIMARY	76	53
142	ADVANCED	PRIMARY	77	55
143	BEGINNER	PRIMARY	78	1
144	BEGINNER	PRIMARY	79	2
145	BEGINNER	SECONDARY	79	5
146	BEGINNER	SECONDARY	79	4
147	INTERMEDIATE	PRIMARY	80	18
148	INTERMEDIATE	PRIMARY	81	17
149	ADVANCED	PRIMARY	82	36
150	INTERMEDIATE	PRIMARY	83	37
151	BEGINNER	SECONDARY	83	36
152	ADVANCED	SECONDARY	83	33
153	BEGINNER	PRIMARY	84	57
154	ADVANCED	PRIMARY	85	53
155	BEGINNER	SECONDARY	85	54
156	ADVANCED	PRIMARY	86	3
157	BEGINNER	SECONDARY	86	5
158	INTERMEDIATE	PRIMARY	87	6
159	INTERMEDIATE	SECONDARY	87	3
160	INTERMEDIATE	SECONDARY	87	5
161	BEGINNER	PRIMARY	88	19
162	INTERMEDIATE	PRIMARY	89	14
163	INTERMEDIATE	SECONDARY	89	18
164	BEGINNER	SECONDARY	89	16
165	ADVANCED	PRIMARY	90	33
166	INTERMEDIATE	PRIMARY	91	33
167	INTERMEDIATE	PRIMARY	92	53
168	ADVANCED	SECONDARY	92	54
169	ADVANCED	SECONDARY	92	56
170	ADVANCED	PRIMARY	93	53
171	ADVANCED	SECONDARY	93	56
172	ADVANCED	SECONDARY	93	52
173	INTERMEDIATE	PRIMARY	94	6
174	INTERMEDIATE	PRIMARY	95	4
175	ADVANCED	SECONDARY	95	3
176	BEGINNER	PRIMARY	96	19
177	INTERMEDIATE	SECONDARY	96	18
178	ADVANCED	SECONDARY	96	17
179	ADVANCED	PRIMARY	97	15
180	BEGINNER	SECONDARY	97	16
181	ADVANCED	SECONDARY	97	14
182	ADVANCED	PRIMARY	98	38
183	ADVANCED	SECONDARY	98	36
184	ADVANCED	SECONDARY	98	33
185	INTERMEDIATE	PRIMARY	99	36
186	INTERMEDIATE	PRIMARY	100	57
187	INTERMEDIATE	PRIMARY	101	52
188	BEGINNER	PRIMARY	102	7
189	INTERMEDIATE	SECONDARY	102	1
190	INTERMEDIATE	PRIMARY	103	4
191	ADVANCED	SECONDARY	103	1
192	ADVANCED	PRIMARY	104	18
193	ADVANCED	SECONDARY	104	17
194	INTERMEDIATE	PRIMARY	105	16
195	BEGINNER	SECONDARY	105	17
196	BEGINNER	PRIMARY	106	36
197	BEGINNER	PRIMARY	107	33
198	INTERMEDIATE	PRIMARY	108	56
199	BEGINNER	PRIMARY	109	56
200	INTERMEDIATE	SECONDARY	109	55
201	ADVANCED	PRIMARY	110	4
202	BEGINNER	SECONDARY	110	1
203	INTERMEDIATE	PRIMARY	111	1
204	ADVANCED	PRIMARY	112	18
205	ADVANCED	SECONDARY	112	15
206	ADVANCED	PRIMARY	113	17
207	INTERMEDIATE	PRIMARY	114	37
208	INTERMEDIATE	SECONDARY	114	36
209	ADVANCED	SECONDARY	114	38
210	ADVANCED	PRIMARY	115	37
211	INTERMEDIATE	SECONDARY	115	34
212	BEGINNER	PRIMARY	116	56
213	ADVANCED	SECONDARY	116	53
214	INTERMEDIATE	SECONDARY	116	55
215	BEGINNER	PRIMARY	117	53
216	BEGINNER	SECONDARY	117	57
217	ADVANCED	PRIMARY	118	4
218	BEGINNER	SECONDARY	118	7
219	INTERMEDIATE	PRIMARY	119	7
220	INTERMEDIATE	PRIMARY	120	19
221	BEGINNER	PRIMARY	121	14
222	ADVANCED	PRIMARY	122	33
223	ADVANCED	SECONDARY	122	36
224	BEGINNER	PRIMARY	123	37
225	ADVANCED	SECONDARY	123	34
226	INTERMEDIATE	SECONDARY	123	36
227	INTERMEDIATE	PRIMARY	124	57
228	INTERMEDIATE	PRIMARY	125	52
229	INTERMEDIATE	PRIMARY	126	4
230	BEGINNER	SECONDARY	126	3
231	INTERMEDIATE	SECONDARY	126	1
232	INTERMEDIATE	PRIMARY	127	7
233	ADVANCED	SECONDARY	127	6
234	INTERMEDIATE	SECONDARY	127	2
235	BEGINNER	PRIMARY	128	19
236	INTERMEDIATE	SECONDARY	128	14
237	ADVANCED	SECONDARY	128	16
238	BEGINNER	PRIMARY	129	18
239	INTERMEDIATE	PRIMARY	130	33
240	ADVANCED	SECONDARY	130	35
241	BEGINNER	SECONDARY	130	36
242	INTERMEDIATE	PRIMARY	131	35
243	BEGINNER	SECONDARY	131	38
244	INTERMEDIATE	PRIMARY	132	53
245	INTERMEDIATE	PRIMARY	133	57
246	INTERMEDIATE	SECONDARY	133	55
247	ADVANCED	SECONDARY	133	52
248	BEGINNER	PRIMARY	134	1
249	BEGINNER	SECONDARY	134	2
250	INTERMEDIATE	PRIMARY	135	4
251	INTERMEDIATE	SECONDARY	135	7
252	ADVANCED	PRIMARY	136	17
253	BEGINNER	SECONDARY	136	19
254	ADVANCED	PRIMARY	137	14
255	BEGINNER	PRIMARY	138	33
256	INTERMEDIATE	SECONDARY	138	34
257	BEGINNER	SECONDARY	138	35
258	ADVANCED	PRIMARY	139	37
259	INTERMEDIATE	SECONDARY	139	38
260	INTERMEDIATE	SECONDARY	139	35
261	ADVANCED	PRIMARY	140	56
262	BEGINNER	SECONDARY	140	55
263	ADVANCED	PRIMARY	141	54
264	ADVANCED	SECONDARY	141	55
265	ADVANCED	PRIMARY	142	7
266	ADVANCED	PRIMARY	143	2
267	BEGINNER	SECONDARY	143	4
268	BEGINNER	SECONDARY	143	6
269	INTERMEDIATE	PRIMARY	144	19
270	INTERMEDIATE	SECONDARY	144	14
271	ADVANCED	PRIMARY	145	18
272	BEGINNER	PRIMARY	146	33
273	INTERMEDIATE	PRIMARY	147	37
274	INTERMEDIATE	SECONDARY	147	34
275	BEGINNER	SECONDARY	147	36
276	ADVANCED	PRIMARY	148	57
277	INTERMEDIATE	SECONDARY	148	53
278	ADVANCED	PRIMARY	149	53
279	BEGINNER	SECONDARY	149	54
280	ADVANCED	PRIMARY	150	4
281	INTERMEDIATE	SECONDARY	150	1
282	ADVANCED	PRIMARY	151	2
283	ADVANCED	SECONDARY	151	4
284	BEGINNER	SECONDARY	151	3
285	INTERMEDIATE	PRIMARY	152	18
286	ADVANCED	SECONDARY	152	19
287	BEGINNER	SECONDARY	152	17
288	INTERMEDIATE	PRIMARY	153	19
289	ADVANCED	SECONDARY	153	18
290	ADVANCED	SECONDARY	153	15
291	ADVANCED	PRIMARY	154	33
292	INTERMEDIATE	PRIMARY	155	33
293	INTERMEDIATE	PRIMARY	156	57
294	BEGINNER	SECONDARY	156	55
295	BEGINNER	SECONDARY	156	54
296	INTERMEDIATE	PRIMARY	157	52
297	BEGINNER	SECONDARY	157	57
298	BEGINNER	PRIMARY	158	6
299	INTERMEDIATE	SECONDARY	158	5
300	BEGINNER	PRIMARY	159	3
301	ADVANCED	SECONDARY	159	1
302	INTERMEDIATE	PRIMARY	160	15
303	ADVANCED	SECONDARY	160	14
304	INTERMEDIATE	PRIMARY	161	15
305	ADVANCED	PRIMARY	162	35
306	ADVANCED	SECONDARY	162	37
307	INTERMEDIATE	SECONDARY	162	38
308	INTERMEDIATE	PRIMARY	163	33
309	INTERMEDIATE	SECONDARY	163	35
310	BEGINNER	SECONDARY	163	38
311	ADVANCED	PRIMARY	164	55
312	BEGINNER	PRIMARY	165	54
313	BEGINNER	PRIMARY	166	1
314	INTERMEDIATE	PRIMARY	167	3
315	INTERMEDIATE	SECONDARY	167	5
316	ADVANCED	SECONDARY	167	7
317	BEGINNER	PRIMARY	168	16
318	ADVANCED	SECONDARY	168	19
319	ADVANCED	PRIMARY	169	19
320	ADVANCED	PRIMARY	170	36
321	ADVANCED	SECONDARY	170	33
322	BEGINNER	SECONDARY	170	34
323	INTERMEDIATE	PRIMARY	171	33
324	ADVANCED	SECONDARY	171	35
325	ADVANCED	SECONDARY	171	38
326	ADVANCED	PRIMARY	172	56
327	INTERMEDIATE	SECONDARY	172	54
328	INTERMEDIATE	PRIMARY	173	54
329	ADVANCED	SECONDARY	173	57
330	BEGINNER	SECONDARY	173	55
331	ADVANCED	PRIMARY	174	2
332	BEGINNER	SECONDARY	174	3
333	ADVANCED	PRIMARY	175	6
334	INTERMEDIATE	SECONDARY	175	3
335	ADVANCED	SECONDARY	175	2
336	INTERMEDIATE	PRIMARY	176	17
337	BEGINNER	PRIMARY	177	18
338	BEGINNER	SECONDARY	177	15
339	ADVANCED	PRIMARY	178	35
340	BEGINNER	PRIMARY	179	33
341	INTERMEDIATE	PRIMARY	180	57
342	ADVANCED	SECONDARY	180	55
343	BEGINNER	PRIMARY	181	56
344	ADVANCED	PRIMARY	182	1
345	INTERMEDIATE	SECONDARY	182	4
346	BEGINNER	SECONDARY	182	2
347	BEGINNER	PRIMARY	183	1
348	BEGINNER	SECONDARY	183	4
349	INTERMEDIATE	SECONDARY	183	5
350	BEGINNER	PRIMARY	184	14
351	INTERMEDIATE	PRIMARY	185	18
352	BEGINNER	SECONDARY	185	19
353	ADVANCED	PRIMARY	186	37
354	INTERMEDIATE	PRIMARY	187	38
355	BEGINNER	SECONDARY	187	37
356	BEGINNER	PRIMARY	188	57
357	ADVANCED	SECONDARY	188	54
358	ADVANCED	PRIMARY	189	52
359	BEGINNER	SECONDARY	189	55
360	BEGINNER	PRIMARY	190	7
361	INTERMEDIATE	SECONDARY	190	3
362	ADVANCED	PRIMARY	191	2
363	BEGINNER	SECONDARY	191	6
364	BEGINNER	PRIMARY	192	19
365	ADVANCED	SECONDARY	192	14
366	INTERMEDIATE	PRIMARY	193	18
367	BEGINNER	SECONDARY	193	16
368	BEGINNER	PRIMARY	194	37
369	INTERMEDIATE	SECONDARY	194	34
370	INTERMEDIATE	PRIMARY	195	35
371	INTERMEDIATE	PRIMARY	196	55
372	BEGINNER	SECONDARY	196	52
373	INTERMEDIATE	SECONDARY	196	54
374	INTERMEDIATE	PRIMARY	197	53
375	INTERMEDIATE	SECONDARY	197	57
376	BEGINNER	SECONDARY	197	56
377	ADVANCED	PRIMARY	198	4
378	INTERMEDIATE	PRIMARY	199	1
379	ADVANCED	SECONDARY	199	5
380	ADVANCED	SECONDARY	199	2
381	ADVANCED	PRIMARY	200	15
382	BEGINNER	SECONDARY	200	19
383	BEGINNER	PRIMARY	201	15
384	ADVANCED	PRIMARY	202	36
385	ADVANCED	SECONDARY	202	38
386	ADVANCED	PRIMARY	203	38
387	INTERMEDIATE	PRIMARY	204	56
388	INTERMEDIATE	SECONDARY	204	52
389	ADVANCED	PRIMARY	205	54
390	BEGINNER	PRIMARY	206	2
391	INTERMEDIATE	SECONDARY	206	6
392	BEGINNER	PRIMARY	207	4
393	ADVANCED	PRIMARY	208	19
394	BEGINNER	SECONDARY	208	14
395	INTERMEDIATE	PRIMARY	209	18
396	BEGINNER	PRIMARY	210	35
397	ADVANCED	SECONDARY	210	36
398	BEGINNER	PRIMARY	211	38
399	ADVANCED	SECONDARY	211	36
400	BEGINNER	SECONDARY	211	37
\.


--
-- Data for Name: monthly_allocations; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.monthly_allocations (id, month, percentage, year, allocation_id) FROM stdin;
1	1	25	2026	1
2	2	50	2026	1
3	3	100	2026	1
4	4	75	2026	1
5	5	50	2026	1
6	6	50	2026	1
7	7	75	2026	1
8	8	25	2026	1
9	9	75	2026	1
10	10	50	2026	1
11	11	100	2026	1
12	12	25	2026	1
13	1	100	2026	2
14	2	100	2026	2
15	3	50	2026	2
16	4	50	2026	2
17	5	50	2026	2
18	6	100	2026	2
19	7	75	2026	2
20	8	50	2026	2
21	9	25	2026	2
22	10	75	2026	2
23	11	50	2026	2
24	12	75	2026	2
25	1	100	2026	3
26	2	50	2026	3
27	3	75	2026	3
28	4	100	2026	3
29	5	100	2026	3
30	6	75	2026	3
31	7	100	2026	3
32	8	75	2026	3
33	9	100	2026	3
34	10	25	2026	3
35	11	100	2026	3
36	12	75	2026	3
37	1	25	2026	4
38	2	75	2026	4
39	3	50	2026	4
40	4	25	2026	4
41	5	100	2026	4
42	6	100	2026	4
43	7	75	2026	4
44	8	50	2026	4
45	9	100	2026	4
46	10	75	2026	4
47	11	75	2026	4
48	12	100	2026	4
49	1	50	2026	5
50	2	75	2026	5
51	3	75	2026	5
52	4	25	2026	5
53	5	75	2026	5
54	6	75	2026	5
55	7	25	2026	5
56	8	75	2026	5
57	9	75	2026	5
58	10	75	2026	5
59	11	25	2026	5
60	12	25	2026	5
61	1	100	2026	6
62	2	75	2026	6
63	3	50	2026	6
64	4	100	2026	6
65	5	75	2026	6
66	6	75	2026	6
67	7	75	2026	6
68	8	50	2026	6
69	9	50	2026	6
70	10	100	2026	6
71	11	50	2026	6
72	12	25	2026	6
73	1	75	2026	7
74	2	75	2026	7
75	3	100	2026	7
76	4	50	2026	7
77	5	50	2026	7
78	6	75	2026	7
79	7	75	2026	7
80	8	50	2026	7
81	9	50	2026	7
82	10	75	2026	7
83	11	100	2026	7
84	12	25	2026	7
85	1	100	2026	8
86	2	50	2026	8
87	3	25	2026	8
88	4	100	2026	8
89	5	75	2026	8
90	6	100	2026	8
91	7	75	2026	8
92	8	100	2026	8
93	9	25	2026	8
94	10	75	2026	8
95	11	75	2026	8
96	12	100	2026	8
97	1	50	2026	9
98	2	25	2026	9
99	3	50	2026	9
100	4	75	2026	9
101	5	50	2026	9
102	6	25	2026	9
103	7	100	2026	9
104	8	25	2026	9
105	9	25	2026	9
106	10	75	2026	9
107	11	75	2026	9
108	12	50	2026	9
109	1	25	2026	10
110	2	75	2026	10
111	3	75	2026	10
112	4	25	2026	10
113	5	25	2026	10
114	6	75	2026	10
115	7	50	2026	10
116	8	100	2026	10
117	9	75	2026	10
118	10	25	2026	10
119	11	75	2026	10
120	12	75	2026	10
121	1	100	2026	11
122	2	50	2026	11
123	3	50	2026	11
124	4	100	2026	11
125	5	75	2026	11
126	6	25	2026	11
127	7	100	2026	11
128	8	75	2026	11
129	9	25	2026	11
130	10	50	2026	11
131	11	25	2026	11
132	12	75	2026	11
133	1	50	2026	12
134	2	75	2026	12
135	3	50	2026	12
136	4	100	2026	12
137	5	50	2026	12
138	6	25	2026	12
139	7	75	2026	12
140	8	25	2026	12
141	9	50	2026	12
142	10	100	2026	12
143	11	100	2026	12
144	12	25	2026	12
145	1	75	2026	13
146	2	50	2026	13
147	3	25	2026	13
148	4	25	2026	13
149	5	50	2026	13
150	6	100	2026	13
151	7	100	2026	13
152	8	25	2026	13
153	9	75	2026	13
154	10	25	2026	13
155	11	100	2026	13
156	12	75	2026	13
157	1	75	2026	14
158	2	75	2026	14
159	3	50	2026	14
160	4	25	2026	14
161	5	100	2026	14
162	6	50	2026	14
163	7	50	2026	14
164	8	50	2026	14
165	9	25	2026	14
166	10	50	2026	14
167	11	100	2026	14
168	12	75	2026	14
169	1	25	2026	15
170	2	50	2026	15
171	3	75	2026	15
172	4	25	2026	15
173	5	50	2026	15
174	6	50	2026	15
175	7	50	2026	15
176	8	75	2026	15
177	9	25	2026	15
178	10	25	2026	15
179	11	25	2026	15
180	12	25	2026	15
181	1	25	2026	16
182	2	25	2026	16
183	3	25	2026	16
184	4	100	2026	16
185	5	50	2026	16
186	6	25	2026	16
187	7	100	2026	16
188	8	100	2026	16
189	9	100	2026	16
190	10	100	2026	16
191	11	50	2026	16
192	12	25	2026	16
193	1	50	2026	17
194	2	75	2026	17
195	3	50	2026	17
196	4	25	2026	17
197	5	50	2026	17
198	6	75	2026	17
199	7	100	2026	17
200	8	100	2026	17
201	9	100	2026	17
202	10	50	2026	17
203	11	25	2026	17
204	12	25	2026	17
205	1	25	2026	18
206	2	75	2026	18
207	3	50	2026	18
208	4	50	2026	18
209	5	50	2026	18
210	6	100	2026	18
211	7	75	2026	18
212	8	25	2026	18
213	9	75	2026	18
214	10	50	2026	18
215	11	50	2026	18
216	12	75	2026	18
217	1	25	2026	19
218	2	25	2026	19
219	3	75	2026	19
220	4	50	2026	19
221	5	75	2026	19
222	6	100	2026	19
223	7	100	2026	19
224	8	50	2026	19
225	9	50	2026	19
226	10	25	2026	19
227	11	100	2026	19
228	12	25	2026	19
229	1	75	2026	20
230	2	25	2026	20
231	3	50	2026	20
232	4	25	2026	20
233	5	100	2026	20
234	6	50	2026	20
235	7	50	2026	20
236	8	100	2026	20
237	9	25	2026	20
238	10	25	2026	20
239	11	25	2026	20
240	12	75	2026	20
241	1	50	2026	21
242	2	75	2026	21
243	3	100	2026	21
244	4	75	2026	21
245	5	50	2026	21
246	6	50	2026	21
247	7	25	2026	21
248	8	50	2026	21
249	9	25	2026	21
250	10	50	2026	21
251	11	75	2026	21
252	12	50	2026	21
253	1	25	2026	22
254	2	50	2026	22
255	3	25	2026	22
256	4	75	2026	22
257	5	75	2026	22
258	6	100	2026	22
259	7	100	2026	22
260	8	25	2026	22
261	9	75	2026	22
262	10	25	2026	22
263	11	50	2026	22
264	12	100	2026	22
265	1	25	2026	23
266	2	75	2026	23
267	3	100	2026	23
268	4	75	2026	23
269	5	75	2026	23
270	6	50	2026	23
271	7	25	2026	23
272	8	50	2026	23
273	9	25	2026	23
274	10	100	2026	23
275	11	100	2026	23
276	12	50	2026	23
277	1	75	2026	24
278	2	75	2026	24
279	3	25	2026	24
280	4	100	2026	24
281	5	25	2026	24
282	6	100	2026	24
283	7	50	2026	24
284	8	75	2026	24
285	9	75	2026	24
286	10	25	2026	24
287	11	50	2026	24
288	12	75	2026	24
289	1	75	2026	25
290	2	50	2026	25
291	3	25	2026	25
292	4	75	2026	25
293	5	100	2026	25
294	6	100	2026	25
295	7	50	2026	25
296	8	100	2026	25
297	9	75	2026	25
298	10	75	2026	25
299	11	75	2026	25
300	12	50	2026	25
301	1	25	2026	26
302	2	75	2026	26
303	3	75	2026	26
304	4	50	2026	26
305	5	75	2026	26
306	6	100	2026	26
307	7	50	2026	26
308	8	100	2026	26
309	9	75	2026	26
310	10	50	2026	26
311	11	25	2026	26
312	12	25	2026	26
313	1	100	2026	27
314	2	75	2026	27
315	3	50	2026	27
316	4	25	2026	27
317	5	25	2026	27
318	6	100	2026	27
319	7	25	2026	27
320	8	75	2026	27
321	9	25	2026	27
322	10	100	2026	27
323	11	75	2026	27
324	12	50	2026	27
325	1	50	2026	28
326	2	100	2026	28
327	3	25	2026	28
328	4	25	2026	28
329	5	50	2026	28
330	6	25	2026	28
331	7	25	2026	28
332	8	100	2026	28
333	9	75	2026	28
334	10	100	2026	28
335	11	75	2026	28
336	12	25	2026	28
337	1	25	2026	29
338	2	50	2026	29
339	3	25	2026	29
340	4	100	2026	29
341	5	100	2026	29
342	6	25	2026	29
343	7	100	2026	29
344	8	75	2026	29
345	9	100	2026	29
346	10	25	2026	29
347	11	75	2026	29
348	12	25	2026	29
349	1	75	2026	30
350	2	25	2026	30
351	3	50	2026	30
352	4	100	2026	30
353	5	25	2026	30
354	6	100	2026	30
355	7	50	2026	30
356	8	100	2026	30
357	9	25	2026	30
358	10	100	2026	30
359	11	50	2026	30
360	12	25	2026	30
361	1	100	2026	31
362	2	50	2026	31
363	3	75	2026	31
364	4	100	2026	31
365	5	100	2026	31
366	6	25	2026	31
367	7	100	2026	31
368	8	100	2026	31
369	9	25	2026	31
370	10	75	2026	31
371	11	75	2026	31
372	12	100	2026	31
373	1	50	2026	32
374	2	75	2026	32
375	3	25	2026	32
376	4	50	2026	32
377	5	75	2026	32
378	6	75	2026	32
379	7	75	2026	32
380	8	50	2026	32
381	9	25	2026	32
382	10	25	2026	32
383	11	50	2026	32
384	12	75	2026	32
385	1	25	2026	33
386	2	100	2026	33
387	3	50	2026	33
388	4	75	2026	33
389	5	75	2026	33
390	6	100	2026	33
391	7	50	2026	33
392	8	75	2026	33
393	9	50	2026	33
394	10	25	2026	33
395	11	25	2026	33
396	12	75	2026	33
397	1	75	2026	34
398	2	25	2026	34
399	3	50	2026	34
400	4	75	2026	34
401	5	25	2026	34
402	6	100	2026	34
403	7	50	2026	34
404	8	100	2026	34
405	9	50	2026	34
406	10	75	2026	34
407	11	50	2026	34
408	12	75	2026	34
409	1	50	2026	35
410	2	25	2026	35
411	3	75	2026	35
412	4	75	2026	35
413	5	75	2026	35
414	6	100	2026	35
415	7	75	2026	35
416	8	100	2026	35
417	9	75	2026	35
418	10	50	2026	35
419	11	50	2026	35
420	12	50	2026	35
421	1	50	2026	36
422	2	50	2026	36
423	3	25	2026	36
424	4	25	2026	36
425	5	25	2026	36
426	6	25	2026	36
427	7	75	2026	36
428	8	25	2026	36
429	9	50	2026	36
430	10	50	2026	36
431	11	25	2026	36
432	12	75	2026	36
433	1	50	2026	37
434	2	100	2026	37
435	3	50	2026	37
436	4	50	2026	37
437	5	50	2026	37
438	6	100	2026	37
439	7	100	2026	37
440	8	100	2026	37
441	9	50	2026	37
442	10	25	2026	37
443	11	75	2026	37
444	12	75	2026	37
445	1	25	2026	38
446	2	50	2026	38
447	3	75	2026	38
448	4	100	2026	38
449	5	100	2026	38
450	6	75	2026	38
451	7	25	2026	38
452	8	50	2026	38
453	9	25	2026	38
454	10	100	2026	38
455	11	75	2026	38
456	12	100	2026	38
457	1	50	2026	39
458	2	100	2026	39
459	3	75	2026	39
460	4	50	2026	39
461	5	50	2026	39
462	6	100	2026	39
463	7	25	2026	39
464	8	100	2026	39
465	9	75	2026	39
466	10	75	2026	39
467	11	25	2026	39
468	12	75	2026	39
469	1	25	2026	40
470	2	75	2026	40
471	3	75	2026	40
472	4	25	2026	40
473	5	100	2026	40
474	6	100	2026	40
475	7	100	2026	40
476	8	25	2026	40
477	9	25	2026	40
478	10	75	2026	40
479	11	75	2026	40
480	12	75	2026	40
481	1	50	2026	41
482	2	75	2026	41
483	3	100	2026	41
484	4	50	2026	41
485	5	50	2026	41
486	6	100	2026	41
487	7	25	2026	41
488	8	50	2026	41
489	9	75	2026	41
490	10	75	2026	41
491	11	100	2026	41
492	12	100	2026	41
493	1	25	2026	42
494	2	25	2026	42
495	3	50	2026	42
496	4	100	2026	42
497	5	75	2026	42
498	6	75	2026	42
499	7	100	2026	42
500	8	25	2026	42
501	9	75	2026	42
502	10	50	2026	42
503	11	50	2026	42
504	12	50	2026	42
505	1	25	2026	43
506	2	50	2026	43
507	3	25	2026	43
508	4	100	2026	43
509	5	25	2026	43
510	6	75	2026	43
511	7	100	2026	43
512	8	100	2026	43
513	9	25	2026	43
514	10	50	2026	43
515	11	25	2026	43
516	12	25	2026	43
517	1	100	2026	44
518	2	75	2026	44
519	3	100	2026	44
520	4	25	2026	44
521	5	100	2026	44
522	6	50	2026	44
523	7	50	2026	44
524	8	100	2026	44
525	9	50	2026	44
526	10	50	2026	44
527	11	25	2026	44
528	12	100	2026	44
529	1	50	2026	45
530	2	100	2026	45
531	3	50	2026	45
532	4	100	2026	45
533	5	75	2026	45
534	6	50	2026	45
535	7	25	2026	45
536	8	75	2026	45
537	9	100	2026	45
538	10	75	2026	45
539	11	75	2026	45
540	12	50	2026	45
541	1	75	2026	46
542	2	50	2026	46
543	3	75	2026	46
544	4	100	2026	46
545	5	25	2026	46
546	6	50	2026	46
547	7	50	2026	46
548	8	75	2026	46
549	9	75	2026	46
550	10	25	2026	46
551	11	50	2026	46
552	12	25	2026	46
553	1	50	2026	47
554	2	100	2026	47
555	3	25	2026	47
556	4	75	2026	47
557	5	50	2026	47
558	6	100	2026	47
559	7	75	2026	47
560	8	25	2026	47
561	9	100	2026	47
562	10	50	2026	47
563	11	100	2026	47
564	12	100	2026	47
565	1	100	2026	48
566	2	75	2026	48
567	3	25	2026	48
568	4	25	2026	48
569	5	25	2026	48
570	6	100	2026	48
571	7	25	2026	48
572	8	100	2026	48
573	9	25	2026	48
574	10	25	2026	48
575	11	100	2026	48
576	12	25	2026	48
577	1	75	2026	49
578	2	100	2026	49
579	3	100	2026	49
580	4	75	2026	49
581	5	25	2026	49
582	6	100	2026	49
583	7	25	2026	49
584	8	75	2026	49
585	9	50	2026	49
586	10	75	2026	49
587	11	75	2026	49
588	12	50	2026	49
589	1	25	2026	50
590	2	50	2026	50
591	3	50	2026	50
592	4	25	2026	50
593	5	75	2026	50
594	6	50	2026	50
595	7	50	2026	50
596	8	100	2026	50
597	9	100	2026	50
598	10	100	2026	50
599	11	50	2026	50
600	12	75	2026	50
601	1	75	2026	51
602	2	25	2026	51
603	3	50	2026	51
604	4	50	2026	51
605	5	50	2026	51
606	6	50	2026	51
607	7	100	2026	51
608	8	75	2026	51
609	9	75	2026	51
610	10	50	2026	51
611	11	100	2026	51
612	12	50	2026	51
613	1	75	2026	52
614	2	50	2026	52
615	3	25	2026	52
616	4	75	2026	52
617	5	75	2026	52
618	6	100	2026	52
619	7	50	2026	52
620	8	50	2026	52
621	9	50	2026	52
622	10	75	2026	52
623	11	50	2026	52
624	12	25	2026	52
625	1	50	2026	53
626	2	75	2026	53
627	3	75	2026	53
628	4	100	2026	53
629	5	100	2026	53
630	6	25	2026	53
631	7	75	2026	53
632	8	75	2026	53
633	9	25	2026	53
634	10	75	2026	53
635	11	75	2026	53
636	12	50	2026	53
637	1	100	2026	54
638	2	75	2026	54
639	3	75	2026	54
640	4	25	2026	54
641	5	100	2026	54
642	6	25	2026	54
643	7	100	2026	54
644	8	50	2026	54
645	9	25	2026	54
646	10	25	2026	54
647	11	100	2026	54
648	12	50	2026	54
649	1	75	2026	55
650	2	100	2026	55
651	3	25	2026	55
652	4	100	2026	55
653	5	50	2026	55
654	6	75	2026	55
655	7	25	2026	55
656	8	75	2026	55
657	9	25	2026	55
658	10	25	2026	55
659	11	50	2026	55
660	12	25	2026	55
661	1	50	2026	56
662	2	25	2026	56
663	3	50	2026	56
664	4	50	2026	56
665	5	50	2026	56
666	6	50	2026	56
667	7	50	2026	56
668	8	25	2026	56
669	9	75	2026	56
670	10	75	2026	56
671	11	25	2026	56
672	12	25	2026	56
673	1	75	2026	57
674	2	75	2026	57
675	3	75	2026	57
676	4	100	2026	57
677	5	25	2026	57
678	6	75	2026	57
679	7	100	2026	57
680	8	100	2026	57
681	9	25	2026	57
682	10	75	2026	57
683	11	75	2026	57
684	12	100	2026	57
685	1	75	2026	58
686	2	25	2026	58
687	3	25	2026	58
688	4	50	2026	58
689	5	100	2026	58
690	6	75	2026	58
691	7	100	2026	58
692	8	25	2026	58
693	9	50	2026	58
694	10	75	2026	58
695	11	50	2026	58
696	12	75	2026	58
697	1	50	2026	59
698	2	50	2026	59
699	3	50	2026	59
700	4	25	2026	59
701	5	75	2026	59
702	6	25	2026	59
703	7	25	2026	59
704	8	100	2026	59
705	9	50	2026	59
706	10	75	2026	59
707	11	75	2026	59
708	12	75	2026	59
709	1	25	2026	60
710	2	100	2026	60
711	3	25	2026	60
712	4	100	2026	60
713	5	100	2026	60
714	6	100	2026	60
715	7	75	2026	60
716	8	25	2026	60
717	9	25	2026	60
718	10	25	2026	60
719	11	100	2026	60
720	12	100	2026	60
721	1	75	2026	61
722	2	100	2026	61
723	3	25	2026	61
724	4	75	2026	61
725	5	100	2026	61
726	6	50	2026	61
727	7	75	2026	61
728	8	50	2026	61
729	9	75	2026	61
730	10	100	2026	61
731	11	75	2026	61
732	12	100	2026	61
733	1	100	2026	62
734	2	50	2026	62
735	3	100	2026	62
736	4	100	2026	62
737	5	100	2026	62
738	6	50	2026	62
739	7	75	2026	62
740	8	25	2026	62
741	9	50	2026	62
742	10	50	2026	62
743	11	100	2026	62
744	12	100	2026	62
745	1	50	2026	63
746	2	25	2026	63
747	3	25	2026	63
748	4	25	2026	63
749	5	50	2026	63
750	6	25	2026	63
751	7	100	2026	63
752	8	75	2026	63
753	9	100	2026	63
754	10	100	2026	63
755	11	100	2026	63
756	12	100	2026	63
757	1	75	2026	64
758	2	100	2026	64
759	3	50	2026	64
760	4	50	2026	64
761	5	100	2026	64
762	6	50	2026	64
763	7	25	2026	64
764	8	75	2026	64
765	9	75	2026	64
766	10	100	2026	64
767	11	100	2026	64
768	12	50	2026	64
769	1	75	2026	65
770	2	100	2026	65
771	3	25	2026	65
772	4	25	2026	65
773	5	25	2026	65
774	6	75	2026	65
775	7	50	2026	65
776	8	25	2026	65
777	9	25	2026	65
778	10	25	2026	65
779	11	75	2026	65
780	12	75	2026	65
781	1	25	2026	66
782	2	75	2026	66
783	3	50	2026	66
784	4	75	2026	66
785	5	50	2026	66
786	6	100	2026	66
787	7	75	2026	66
788	8	50	2026	66
789	9	75	2026	66
790	10	25	2026	66
791	11	50	2026	66
792	12	50	2026	66
793	1	50	2026	67
794	2	100	2026	67
795	3	50	2026	67
796	4	25	2026	67
797	5	50	2026	67
798	6	50	2026	67
799	7	25	2026	67
800	8	50	2026	67
801	9	75	2026	67
802	10	50	2026	67
803	11	100	2026	67
804	12	50	2026	67
805	1	100	2026	68
806	2	75	2026	68
807	3	100	2026	68
808	4	100	2026	68
809	5	25	2026	68
810	6	75	2026	68
811	7	100	2026	68
812	8	100	2026	68
813	9	50	2026	68
814	10	75	2026	68
815	11	25	2026	68
816	12	50	2026	68
817	1	100	2026	69
818	2	75	2026	69
819	3	75	2026	69
820	4	50	2026	69
821	5	100	2026	69
822	6	25	2026	69
823	7	50	2026	69
824	8	75	2026	69
825	9	25	2026	69
826	10	75	2026	69
827	11	100	2026	69
828	12	100	2026	69
829	1	50	2026	70
830	2	100	2026	70
831	3	25	2026	70
832	4	25	2026	70
833	5	50	2026	70
834	6	100	2026	70
835	7	25	2026	70
836	8	50	2026	70
837	9	75	2026	70
838	10	25	2026	70
839	11	25	2026	70
840	12	50	2026	70
841	1	25	2026	71
842	2	75	2026	71
843	3	75	2026	71
844	4	50	2026	71
845	5	100	2026	71
846	6	100	2026	71
847	7	25	2026	71
848	8	50	2026	71
849	9	100	2026	71
850	10	25	2026	71
851	11	25	2026	71
852	12	50	2026	71
853	1	100	2026	72
854	2	100	2026	72
855	3	50	2026	72
856	4	25	2026	72
857	5	25	2026	72
858	6	100	2026	72
859	7	75	2026	72
860	8	25	2026	72
861	9	75	2026	72
862	10	25	2026	72
863	11	100	2026	72
864	12	50	2026	72
865	1	75	2026	73
866	2	75	2026	73
867	3	50	2026	73
868	4	100	2026	73
869	5	100	2026	73
870	6	75	2026	73
871	7	100	2026	73
872	8	75	2026	73
873	9	50	2026	73
874	10	25	2026	73
875	11	50	2026	73
876	12	25	2026	73
877	1	25	2026	74
878	2	50	2026	74
879	3	25	2026	74
880	4	25	2026	74
881	5	50	2026	74
882	6	75	2026	74
883	7	50	2026	74
884	8	25	2026	74
885	9	75	2026	74
886	10	75	2026	74
887	11	100	2026	74
888	12	50	2026	74
889	1	75	2026	75
890	2	50	2026	75
891	3	25	2026	75
892	4	100	2026	75
893	5	100	2026	75
894	6	50	2026	75
895	7	100	2026	75
896	8	75	2026	75
897	9	100	2026	75
898	10	75	2026	75
899	11	50	2026	75
900	12	75	2026	75
901	1	75	2026	76
902	2	75	2026	76
903	3	75	2026	76
904	4	25	2026	76
905	5	50	2026	76
906	6	75	2026	76
907	7	50	2026	76
908	8	50	2026	76
909	9	100	2026	76
910	10	25	2026	76
911	11	75	2026	76
912	12	75	2026	76
913	1	100	2026	77
914	2	50	2026	77
915	3	25	2026	77
916	4	50	2026	77
917	5	100	2026	77
918	6	75	2026	77
919	7	75	2026	77
920	8	25	2026	77
921	9	100	2026	77
922	10	100	2026	77
923	11	50	2026	77
924	12	50	2026	77
925	1	100	2026	78
926	2	25	2026	78
927	3	75	2026	78
928	4	50	2026	78
929	5	75	2026	78
930	6	50	2026	78
931	7	100	2026	78
932	8	75	2026	78
933	9	75	2026	78
934	10	50	2026	78
935	11	100	2026	78
936	12	100	2026	78
937	1	50	2026	79
938	2	100	2026	79
939	3	25	2026	79
940	4	75	2026	79
941	5	100	2026	79
942	6	75	2026	79
943	7	75	2026	79
944	8	25	2026	79
945	9	75	2026	79
946	10	75	2026	79
947	11	100	2026	79
948	12	25	2026	79
949	1	25	2026	80
950	2	50	2026	80
951	3	75	2026	80
952	4	25	2026	80
953	5	75	2026	80
954	6	25	2026	80
955	7	100	2026	80
956	8	100	2026	80
957	9	100	2026	80
958	10	50	2026	80
959	11	50	2026	80
960	12	50	2026	80
961	1	100	2026	81
962	2	25	2026	81
963	3	75	2026	81
964	4	75	2026	81
965	5	100	2026	81
966	6	75	2026	81
967	7	100	2026	81
968	8	25	2026	81
969	9	25	2026	81
970	10	25	2026	81
971	11	25	2026	81
972	12	100	2026	81
973	1	25	2026	82
974	2	75	2026	82
975	3	50	2026	82
976	4	100	2026	82
977	5	100	2026	82
978	6	100	2026	82
979	7	100	2026	82
980	8	75	2026	82
981	9	25	2026	82
982	10	50	2026	82
983	11	25	2026	82
984	12	50	2026	82
985	1	100	2026	83
986	2	25	2026	83
987	3	100	2026	83
988	4	100	2026	83
989	5	75	2026	83
990	6	50	2026	83
991	7	100	2026	83
992	8	100	2026	83
993	9	50	2026	83
994	10	50	2026	83
995	11	75	2026	83
996	12	75	2026	83
997	1	75	2026	84
998	2	50	2026	84
999	3	25	2026	84
1000	4	75	2026	84
1001	5	50	2026	84
1002	6	100	2026	84
1003	7	100	2026	84
1004	8	75	2026	84
1005	9	100	2026	84
1006	10	50	2026	84
1007	11	100	2026	84
1008	12	75	2026	84
1009	1	100	2026	85
1010	2	50	2026	85
1011	3	50	2026	85
1012	4	25	2026	85
1013	5	100	2026	85
1014	6	75	2026	85
1015	7	50	2026	85
1016	8	50	2026	85
1017	9	100	2026	85
1018	10	25	2026	85
1019	11	50	2026	85
1020	12	50	2026	85
1021	1	75	2026	86
1022	2	100	2026	86
1023	3	100	2026	86
1024	4	50	2026	86
1025	5	50	2026	86
1026	6	75	2026	86
1027	7	50	2026	86
1028	8	25	2026	86
1029	9	50	2026	86
1030	10	75	2026	86
1031	11	100	2026	86
1032	12	25	2026	86
1033	1	25	2026	87
1034	2	100	2026	87
1035	3	50	2026	87
1036	4	50	2026	87
1037	5	75	2026	87
1038	6	75	2026	87
1039	7	100	2026	87
1040	8	25	2026	87
1041	9	75	2026	87
1042	10	75	2026	87
1043	11	25	2026	87
1044	12	100	2026	87
1045	1	75	2026	88
1046	2	25	2026	88
1047	3	100	2026	88
1048	4	100	2026	88
1049	5	100	2026	88
1050	6	75	2026	88
1051	7	25	2026	88
1052	8	100	2026	88
1053	9	100	2026	88
1054	10	100	2026	88
1055	11	100	2026	88
1056	12	50	2026	88
1057	1	100	2026	89
1058	2	25	2026	89
1059	3	25	2026	89
1060	4	100	2026	89
1061	5	25	2026	89
1062	6	50	2026	89
1063	7	100	2026	89
1064	8	25	2026	89
1065	9	75	2026	89
1066	10	75	2026	89
1067	11	75	2026	89
1068	12	75	2026	89
1069	1	100	2026	90
1070	2	25	2026	90
1071	3	75	2026	90
1072	4	75	2026	90
1073	5	75	2026	90
1074	6	25	2026	90
1075	7	25	2026	90
1076	8	75	2026	90
1077	9	75	2026	90
1078	10	50	2026	90
1079	11	50	2026	90
1080	12	75	2026	90
1081	1	100	2026	91
1082	2	25	2026	91
1083	3	25	2026	91
1084	4	75	2026	91
1085	5	25	2026	91
1086	6	50	2026	91
1087	7	50	2026	91
1088	8	75	2026	91
1089	9	100	2026	91
1090	10	25	2026	91
1091	11	50	2026	91
1092	12	50	2026	91
1093	1	75	2026	92
1094	2	25	2026	92
1095	3	75	2026	92
1096	4	75	2026	92
1097	5	75	2026	92
1098	6	100	2026	92
1099	7	100	2026	92
1100	8	75	2026	92
1101	9	100	2026	92
1102	10	100	2026	92
1103	11	25	2026	92
1104	12	50	2026	92
1105	1	50	2026	93
1106	2	25	2026	93
1107	3	100	2026	93
1108	4	100	2026	93
1109	5	100	2026	93
1110	6	25	2026	93
1111	7	100	2026	93
1112	8	75	2026	93
1113	9	50	2026	93
1114	10	75	2026	93
1115	11	50	2026	93
1116	12	100	2026	93
1117	1	100	2026	94
1118	2	25	2026	94
1119	3	100	2026	94
1120	4	75	2026	94
1121	5	75	2026	94
1122	6	50	2026	94
1123	7	75	2026	94
1124	8	75	2026	94
1125	9	25	2026	94
1126	10	25	2026	94
1127	11	25	2026	94
1128	12	25	2026	94
1129	1	75	2026	95
1130	2	25	2026	95
1131	3	75	2026	95
1132	4	75	2026	95
1133	5	100	2026	95
1134	6	100	2026	95
1135	7	50	2026	95
1136	8	50	2026	95
1137	9	75	2026	95
1138	10	25	2026	95
1139	11	100	2026	95
1140	12	75	2026	95
1141	1	25	2026	96
1142	2	25	2026	96
1143	3	25	2026	96
1144	4	75	2026	96
1145	5	75	2026	96
1146	6	25	2026	96
1147	7	75	2026	96
1148	8	50	2026	96
1149	9	75	2026	96
1150	10	75	2026	96
1151	11	50	2026	96
1152	12	75	2026	96
1153	1	25	2026	97
1154	2	25	2026	97
1155	3	100	2026	97
1156	4	25	2026	97
1157	5	75	2026	97
1158	6	100	2026	97
1159	7	50	2026	97
1160	8	50	2026	97
1161	9	50	2026	97
1162	10	100	2026	97
1163	11	100	2026	97
1164	12	100	2026	97
1165	1	100	2026	98
1166	2	50	2026	98
1167	3	75	2026	98
1168	4	25	2026	98
1169	5	25	2026	98
1170	6	100	2026	98
1171	7	75	2026	98
1172	8	100	2026	98
1173	9	50	2026	98
1174	10	50	2026	98
1175	11	75	2026	98
1176	12	100	2026	98
1177	1	75	2026	99
1178	2	25	2026	99
1179	3	25	2026	99
1180	4	25	2026	99
1181	5	100	2026	99
1182	6	50	2026	99
1183	7	25	2026	99
1184	8	50	2026	99
1185	9	100	2026	99
1186	10	50	2026	99
1187	11	75	2026	99
1188	12	50	2026	99
1189	1	75	2026	100
1190	2	75	2026	100
1191	3	75	2026	100
1192	4	100	2026	100
1193	5	75	2026	100
1194	6	50	2026	100
1195	7	25	2026	100
1196	8	50	2026	100
1197	9	100	2026	100
1198	10	50	2026	100
1199	11	100	2026	100
1200	12	75	2026	100
1201	1	50	2026	101
1202	2	75	2026	101
1203	3	50	2026	101
1204	4	25	2026	101
1205	5	100	2026	101
1206	6	50	2026	101
1207	7	50	2026	101
1208	8	75	2026	101
1209	9	50	2026	101
1210	10	25	2026	101
1211	11	100	2026	101
1212	12	75	2026	101
1213	1	75	2026	102
1214	2	25	2026	102
1215	3	100	2026	102
1216	4	25	2026	102
1217	5	75	2026	102
1218	6	75	2026	102
1219	7	25	2026	102
1220	8	100	2026	102
1221	9	25	2026	102
1222	10	25	2026	102
1223	11	75	2026	102
1224	12	25	2026	102
1225	1	25	2026	103
1226	2	100	2026	103
1227	3	25	2026	103
1228	4	100	2026	103
1229	5	25	2026	103
1230	6	50	2026	103
1231	7	75	2026	103
1232	8	25	2026	103
1233	9	100	2026	103
1234	10	100	2026	103
1235	11	100	2026	103
1236	12	50	2026	103
1237	1	50	2026	104
1238	2	25	2026	104
1239	3	50	2026	104
1240	4	100	2026	104
1241	5	75	2026	104
1242	6	75	2026	104
1243	7	75	2026	104
1244	8	50	2026	104
1245	9	25	2026	104
1246	10	25	2026	104
1247	11	75	2026	104
1248	12	75	2026	104
1249	1	25	2026	105
1250	2	25	2026	105
1251	3	25	2026	105
1252	4	75	2026	105
1253	5	50	2026	105
1254	6	25	2026	105
1255	7	50	2026	105
1256	8	25	2026	105
1257	9	50	2026	105
1258	10	75	2026	105
1259	11	75	2026	105
1260	12	25	2026	105
1261	1	50	2026	106
1262	2	75	2026	106
1263	3	75	2026	106
1264	4	25	2026	106
1265	5	75	2026	106
1266	6	50	2026	106
1267	7	75	2026	106
1268	8	50	2026	106
1269	9	25	2026	106
1270	10	75	2026	106
1271	11	75	2026	106
1272	12	25	2026	106
1273	1	25	2026	107
1274	2	75	2026	107
1275	3	50	2026	107
1276	4	75	2026	107
1277	5	100	2026	107
1278	6	100	2026	107
1279	7	50	2026	107
1280	8	50	2026	107
1281	9	100	2026	107
1282	10	75	2026	107
1283	11	50	2026	107
1284	12	50	2026	107
1285	1	25	2026	108
1286	2	75	2026	108
1287	3	25	2026	108
1288	4	25	2026	108
1289	5	100	2026	108
1290	6	25	2026	108
1291	7	50	2026	108
1292	8	50	2026	108
1293	9	25	2026	108
1294	10	75	2026	108
1295	11	50	2026	108
1296	12	50	2026	108
1297	1	100	2026	109
1298	2	50	2026	109
1299	3	75	2026	109
1300	4	75	2026	109
1301	5	50	2026	109
1302	6	75	2026	109
1303	7	100	2026	109
1304	8	50	2026	109
1305	9	50	2026	109
1306	10	100	2026	109
1307	11	25	2026	109
1308	12	25	2026	109
1309	1	50	2026	110
1310	2	50	2026	110
1311	3	50	2026	110
1312	4	25	2026	110
1313	5	50	2026	110
1314	6	25	2026	110
1315	7	100	2026	110
1316	8	50	2026	110
1317	9	50	2026	110
1318	10	50	2026	110
1319	11	100	2026	110
1320	12	100	2026	110
1321	1	75	2026	111
1322	2	25	2026	111
1323	3	50	2026	111
1324	4	100	2026	111
1325	5	100	2026	111
1326	6	75	2026	111
1327	7	75	2026	111
1328	8	25	2026	111
1329	9	75	2026	111
1330	10	50	2026	111
1331	11	75	2026	111
1332	12	25	2026	111
1333	1	100	2026	112
1334	2	25	2026	112
1335	3	25	2026	112
1336	4	50	2026	112
1337	5	25	2026	112
1338	6	100	2026	112
1339	7	75	2026	112
1340	8	100	2026	112
1341	9	100	2026	112
1342	10	50	2026	112
1343	11	50	2026	112
1344	12	50	2026	112
1345	1	50	2026	113
1346	2	100	2026	113
1347	3	50	2026	113
1348	4	75	2026	113
1349	5	25	2026	113
1350	6	100	2026	113
1351	7	50	2026	113
1352	8	100	2026	113
1353	9	25	2026	113
1354	10	100	2026	113
1355	11	75	2026	113
1356	12	75	2026	113
1357	1	75	2026	114
1358	2	100	2026	114
1359	3	50	2026	114
1360	4	50	2026	114
1361	5	75	2026	114
1362	6	75	2026	114
1363	7	100	2026	114
1364	8	25	2026	114
1365	9	75	2026	114
1366	10	75	2026	114
1367	11	75	2026	114
1368	12	25	2026	114
1369	1	50	2026	115
1370	2	100	2026	115
1371	3	50	2026	115
1372	4	100	2026	115
1373	5	100	2026	115
1374	6	50	2026	115
1375	7	50	2026	115
1376	8	50	2026	115
1377	9	100	2026	115
1378	10	75	2026	115
1379	11	50	2026	115
1380	12	25	2026	115
1381	1	50	2026	116
1382	2	75	2026	116
1383	3	25	2026	116
1384	4	75	2026	116
1385	5	50	2026	116
1386	6	25	2026	116
1387	7	25	2026	116
1388	8	75	2026	116
1389	9	100	2026	116
1390	10	100	2026	116
1391	11	75	2026	116
1392	12	75	2026	116
1393	1	50	2026	117
1394	2	25	2026	117
1395	3	25	2026	117
1396	4	50	2026	117
1397	5	75	2026	117
1398	6	50	2026	117
1399	7	50	2026	117
1400	8	100	2026	117
1401	9	75	2026	117
1402	10	75	2026	117
1403	11	100	2026	117
1404	12	25	2026	117
1405	1	50	2026	118
1406	2	25	2026	118
1407	3	75	2026	118
1408	4	100	2026	118
1409	5	25	2026	118
1410	6	25	2026	118
1411	7	100	2026	118
1412	8	75	2026	118
1413	9	25	2026	118
1414	10	50	2026	118
1415	11	25	2026	118
1416	12	75	2026	118
1417	1	25	2026	119
1418	2	100	2026	119
1419	3	25	2026	119
1420	4	75	2026	119
1421	5	100	2026	119
1422	6	75	2026	119
1423	7	50	2026	119
1424	8	50	2026	119
1425	9	25	2026	119
1426	10	25	2026	119
1427	11	75	2026	119
1428	12	100	2026	119
1429	1	100	2026	120
1430	2	100	2026	120
1431	3	75	2026	120
1432	4	100	2026	120
1433	5	25	2026	120
1434	6	75	2026	120
1435	7	75	2026	120
1436	8	50	2026	120
1437	9	75	2026	120
1438	10	100	2026	120
1439	11	25	2026	120
1440	12	50	2026	120
1441	1	100	2026	121
1442	2	25	2026	121
1443	3	50	2026	121
1444	4	25	2026	121
1445	5	100	2026	121
1446	6	25	2026	121
1447	7	75	2026	121
1448	8	100	2026	121
1449	9	25	2026	121
1450	10	75	2026	121
1451	11	50	2026	121
1452	12	75	2026	121
1453	1	25	2026	122
1454	2	100	2026	122
1455	3	75	2026	122
1456	4	25	2026	122
1457	5	100	2026	122
1458	6	25	2026	122
1459	7	50	2026	122
1460	8	100	2026	122
1461	9	75	2026	122
1462	10	50	2026	122
1463	11	100	2026	122
1464	12	100	2026	122
1465	1	25	2026	123
1466	2	25	2026	123
1467	3	50	2026	123
1468	4	25	2026	123
1469	5	100	2026	123
1470	6	75	2026	123
1471	7	75	2026	123
1472	8	25	2026	123
1473	9	75	2026	123
1474	10	100	2026	123
1475	11	75	2026	123
1476	12	25	2026	123
1477	1	50	2026	124
1478	2	75	2026	124
1479	3	75	2026	124
1480	4	100	2026	124
1481	5	50	2026	124
1482	6	75	2026	124
1483	7	50	2026	124
1484	8	75	2026	124
1485	9	50	2026	124
1486	10	25	2026	124
1487	11	75	2026	124
1488	12	50	2026	124
1489	1	75	2026	125
1490	2	100	2026	125
1491	3	50	2026	125
1492	4	50	2026	125
1493	5	50	2026	125
1494	6	100	2026	125
1495	7	25	2026	125
1496	8	75	2026	125
1497	9	50	2026	125
1498	10	75	2026	125
1499	11	25	2026	125
1500	12	100	2026	125
1501	1	50	2026	126
1502	2	25	2026	126
1503	3	25	2026	126
1504	4	100	2026	126
1505	5	75	2026	126
1506	6	25	2026	126
1507	7	75	2026	126
1508	8	100	2026	126
1509	9	50	2026	126
1510	10	75	2026	126
1511	11	50	2026	126
1512	12	50	2026	126
1513	1	75	2026	127
1514	2	100	2026	127
1515	3	75	2026	127
1516	4	100	2026	127
1517	5	75	2026	127
1518	6	75	2026	127
1519	7	25	2026	127
1520	8	100	2026	127
1521	9	100	2026	127
1522	10	100	2026	127
1523	11	75	2026	127
1524	12	75	2026	127
1525	1	50	2026	128
1526	2	25	2026	128
1527	3	75	2026	128
1528	4	75	2026	128
1529	5	50	2026	128
1530	6	100	2026	128
1531	7	75	2026	128
1532	8	100	2026	128
1533	9	100	2026	128
1534	10	100	2026	128
1535	11	25	2026	128
1536	12	25	2026	128
1537	1	75	2026	129
1538	2	100	2026	129
1539	3	100	2026	129
1540	4	25	2026	129
1541	5	25	2026	129
1542	6	75	2026	129
1543	7	50	2026	129
1544	8	25	2026	129
1545	9	50	2026	129
1546	10	25	2026	129
1547	11	100	2026	129
1548	12	50	2026	129
1549	1	50	2026	130
1550	2	75	2026	130
1551	3	100	2026	130
1552	4	75	2026	130
1553	5	100	2026	130
1554	6	50	2026	130
1555	7	100	2026	130
1556	8	100	2026	130
1557	9	50	2026	130
1558	10	75	2026	130
1559	11	75	2026	130
1560	12	75	2026	130
1561	1	100	2026	131
1562	2	25	2026	131
1563	3	50	2026	131
1564	4	50	2026	131
1565	5	25	2026	131
1566	6	25	2026	131
1567	7	75	2026	131
1568	8	75	2026	131
1569	9	50	2026	131
1570	10	100	2026	131
1571	11	100	2026	131
1572	12	25	2026	131
1573	1	100	2026	132
1574	2	50	2026	132
1575	3	75	2026	132
1576	4	75	2026	132
1577	5	100	2026	132
1578	6	50	2026	132
1579	7	25	2026	132
1580	8	75	2026	132
1581	9	100	2026	132
1582	10	50	2026	132
1583	11	100	2026	132
1584	12	100	2026	132
1585	1	50	2026	133
1586	2	50	2026	133
1587	3	100	2026	133
1588	4	100	2026	133
1589	5	75	2026	133
1590	6	25	2026	133
1591	7	25	2026	133
1592	8	50	2026	133
1593	9	100	2026	133
1594	10	50	2026	133
1595	11	50	2026	133
1596	12	50	2026	133
1597	1	75	2026	134
1598	2	25	2026	134
1599	3	25	2026	134
1600	4	100	2026	134
1601	5	75	2026	134
1602	6	100	2026	134
1603	7	100	2026	134
1604	8	50	2026	134
1605	9	100	2026	134
1606	10	50	2026	134
1607	11	50	2026	134
1608	12	50	2026	134
1609	1	25	2026	135
1610	2	75	2026	135
1611	3	25	2026	135
1612	4	25	2026	135
1613	5	100	2026	135
1614	6	75	2026	135
1615	7	25	2026	135
1616	8	75	2026	135
1617	9	100	2026	135
1618	10	50	2026	135
1619	11	25	2026	135
1620	12	50	2026	135
1621	1	25	2026	136
1622	2	50	2026	136
1623	3	100	2026	136
1624	4	25	2026	136
1625	5	50	2026	136
1626	6	50	2026	136
1627	7	50	2026	136
1628	8	50	2026	136
1629	9	100	2026	136
1630	10	25	2026	136
1631	11	50	2026	136
1632	12	100	2026	136
1633	1	100	2026	137
1634	2	75	2026	137
1635	3	100	2026	137
1636	4	75	2026	137
1637	5	75	2026	137
1638	6	100	2026	137
1639	7	25	2026	137
1640	8	75	2026	137
1641	9	25	2026	137
1642	10	75	2026	137
1643	11	75	2026	137
1644	12	100	2026	137
1645	1	50	2026	138
1646	2	25	2026	138
1647	3	25	2026	138
1648	4	100	2026	138
1649	5	25	2026	138
1650	6	75	2026	138
1651	7	50	2026	138
1652	8	75	2026	138
1653	9	75	2026	138
1654	10	75	2026	138
1655	11	75	2026	138
1656	12	100	2026	138
1657	1	50	2026	139
1658	2	100	2026	139
1659	3	50	2026	139
1660	4	50	2026	139
1661	5	75	2026	139
1662	6	50	2026	139
1663	7	75	2026	139
1664	8	50	2026	139
1665	9	25	2026	139
1666	10	75	2026	139
1667	11	100	2026	139
1668	12	25	2026	139
1669	1	25	2026	140
1670	2	25	2026	140
1671	3	50	2026	140
1672	4	50	2026	140
1673	5	100	2026	140
1674	6	50	2026	140
1675	7	50	2026	140
1676	8	75	2026	140
1677	9	50	2026	140
1678	10	50	2026	140
1679	11	100	2026	140
1680	12	100	2026	140
1681	1	25	2026	141
1682	2	50	2026	141
1683	3	75	2026	141
1684	4	25	2026	141
1685	5	75	2026	141
1686	6	50	2026	141
1687	7	75	2026	141
1688	8	25	2026	141
1689	9	50	2026	141
1690	10	25	2026	141
1691	11	75	2026	141
1692	12	25	2026	141
1693	1	25	2026	142
1694	2	100	2026	142
1695	3	25	2026	142
1696	4	25	2026	142
1697	5	75	2026	142
1698	6	75	2026	142
1699	7	25	2026	142
1700	8	25	2026	142
1701	9	100	2026	142
1702	10	50	2026	142
1703	11	100	2026	142
1704	12	50	2026	142
1705	1	50	2026	143
1706	2	25	2026	143
1707	3	25	2026	143
1708	4	75	2026	143
1709	5	50	2026	143
1710	6	25	2026	143
1711	7	25	2026	143
1712	8	50	2026	143
1713	9	75	2026	143
1714	10	75	2026	143
1715	11	100	2026	143
1716	12	75	2026	143
1717	1	75	2026	144
1718	2	25	2026	144
1719	3	75	2026	144
1720	4	25	2026	144
1721	5	75	2026	144
1722	6	50	2026	144
1723	7	75	2026	144
1724	8	50	2026	144
1725	9	25	2026	144
1726	10	75	2026	144
1727	11	50	2026	144
1728	12	75	2026	144
1729	1	100	2026	145
1730	2	75	2026	145
1731	3	50	2026	145
1732	4	50	2026	145
1733	5	25	2026	145
1734	6	75	2026	145
1735	7	25	2026	145
1736	8	25	2026	145
1737	9	50	2026	145
1738	10	75	2026	145
1739	11	50	2026	145
1740	12	100	2026	145
1741	1	25	2026	146
1742	2	50	2026	146
1743	3	75	2026	146
1744	4	75	2026	146
1745	5	100	2026	146
1746	6	75	2026	146
1747	7	100	2026	146
1748	8	75	2026	146
1749	9	100	2026	146
1750	10	25	2026	146
1751	11	75	2026	146
1752	12	25	2026	146
1753	1	100	2026	147
1754	2	50	2026	147
1755	3	50	2026	147
1756	4	25	2026	147
1757	5	75	2026	147
1758	6	75	2026	147
1759	7	75	2026	147
1760	8	25	2026	147
1761	9	100	2026	147
1762	10	50	2026	147
1763	11	75	2026	147
1764	12	25	2026	147
1765	1	25	2026	148
1766	2	50	2026	148
1767	3	100	2026	148
1768	4	75	2026	148
1769	5	25	2026	148
1770	6	50	2026	148
1771	7	100	2026	148
1772	8	100	2026	148
1773	9	75	2026	148
1774	10	25	2026	148
1775	11	75	2026	148
1776	12	50	2026	148
1777	1	100	2026	149
1778	2	100	2026	149
1779	3	100	2026	149
1780	4	25	2026	149
1781	5	25	2026	149
1782	6	25	2026	149
1783	7	100	2026	149
1784	8	75	2026	149
1785	9	75	2026	149
1786	10	75	2026	149
1787	11	50	2026	149
1788	12	75	2026	149
1789	1	75	2026	150
1790	2	25	2026	150
1791	3	100	2026	150
1792	4	100	2026	150
1793	5	75	2026	150
1794	6	100	2026	150
1795	7	50	2026	150
1796	8	50	2026	150
1797	9	75	2026	150
1798	10	100	2026	150
1799	11	25	2026	150
1800	12	25	2026	150
1801	1	100	2026	151
1802	2	25	2026	151
1803	3	75	2026	151
1804	4	75	2026	151
1805	5	25	2026	151
1806	6	50	2026	151
1807	7	100	2026	151
1808	8	75	2026	151
1809	9	75	2026	151
1810	10	50	2026	151
1811	11	50	2026	151
1812	12	50	2026	151
1813	1	25	2026	152
1814	2	50	2026	152
1815	3	25	2026	152
1816	4	25	2026	152
1817	5	25	2026	152
1818	6	50	2026	152
1819	7	25	2026	152
1820	8	100	2026	152
1821	9	100	2026	152
1822	10	75	2026	152
1823	11	50	2026	152
1824	12	25	2026	152
1825	1	100	2026	153
1826	2	50	2026	153
1827	3	100	2026	153
1828	4	100	2026	153
1829	5	100	2026	153
1830	6	50	2026	153
1831	7	75	2026	153
1832	8	75	2026	153
1833	9	100	2026	153
1834	10	100	2026	153
1835	11	50	2026	153
1836	12	25	2026	153
1837	1	50	2026	154
1838	2	100	2026	154
1839	3	25	2026	154
1840	4	75	2026	154
1841	5	25	2026	154
1842	6	25	2026	154
1843	7	100	2026	154
1844	8	25	2026	154
1845	9	50	2026	154
1846	10	75	2026	154
1847	11	100	2026	154
1848	12	100	2026	154
1849	1	100	2026	155
1850	2	75	2026	155
1851	3	50	2026	155
1852	4	100	2026	155
1853	5	75	2026	155
1854	6	100	2026	155
1855	7	25	2026	155
1856	8	75	2026	155
1857	9	75	2026	155
1858	10	25	2026	155
1859	11	75	2026	155
1860	12	25	2026	155
1861	1	50	2026	156
1862	2	25	2026	156
1863	3	50	2026	156
1864	4	75	2026	156
1865	5	100	2026	156
1866	6	75	2026	156
1867	7	75	2026	156
1868	8	100	2026	156
1869	9	75	2026	156
1870	10	25	2026	156
1871	11	100	2026	156
1872	12	25	2026	156
1873	1	100	2026	157
1874	2	25	2026	157
1875	3	50	2026	157
1876	4	25	2026	157
1877	5	25	2026	157
1878	6	100	2026	157
1879	7	75	2026	157
1880	8	25	2026	157
1881	9	50	2026	157
1882	10	50	2026	157
1883	11	100	2026	157
1884	12	50	2026	157
1885	1	100	2026	158
1886	2	100	2026	158
1887	3	50	2026	158
1888	4	75	2026	158
1889	5	75	2026	158
1890	6	100	2026	158
1891	7	50	2026	158
1892	8	75	2026	158
1893	9	75	2026	158
1894	10	25	2026	158
1895	11	100	2026	158
1896	12	25	2026	158
1897	1	100	2026	159
1898	2	25	2026	159
1899	3	25	2026	159
1900	4	50	2026	159
1901	5	75	2026	159
1902	6	25	2026	159
1903	7	100	2026	159
1904	8	50	2026	159
1905	9	100	2026	159
1906	10	75	2026	159
1907	11	100	2026	159
1908	12	75	2026	159
1909	1	75	2026	160
1910	2	75	2026	160
1911	3	50	2026	160
1912	4	25	2026	160
1913	5	25	2026	160
1914	6	50	2026	160
1915	7	50	2026	160
1916	8	75	2026	160
1917	9	75	2026	160
1918	10	75	2026	160
1919	11	75	2026	160
1920	12	100	2026	160
1921	1	25	2026	161
1922	2	75	2026	161
1923	3	100	2026	161
1924	4	50	2026	161
1925	5	25	2026	161
1926	6	25	2026	161
1927	7	25	2026	161
1928	8	50	2026	161
1929	9	75	2026	161
1930	10	100	2026	161
1931	11	75	2026	161
1932	12	75	2026	161
1933	1	50	2026	162
1934	2	50	2026	162
1935	3	75	2026	162
1936	4	75	2026	162
1937	5	25	2026	162
1938	6	25	2026	162
1939	7	25	2026	162
1940	8	75	2026	162
1941	9	75	2026	162
1942	10	25	2026	162
1943	11	50	2026	162
1944	12	25	2026	162
1945	1	100	2026	163
1946	2	25	2026	163
1947	3	100	2026	163
1948	4	50	2026	163
1949	5	50	2026	163
1950	6	75	2026	163
1951	7	50	2026	163
1952	8	100	2026	163
1953	9	100	2026	163
1954	10	25	2026	163
1955	11	75	2026	163
1956	12	25	2026	163
1957	1	50	2026	164
1958	2	75	2026	164
1959	3	25	2026	164
1960	4	100	2026	164
1961	5	25	2026	164
1962	6	100	2026	164
1963	7	25	2026	164
1964	8	100	2026	164
1965	9	25	2026	164
1966	10	75	2026	164
1967	11	25	2026	164
1968	12	50	2026	164
1969	1	25	2026	165
1970	2	25	2026	165
1971	3	75	2026	165
1972	4	25	2026	165
1973	5	25	2026	165
1974	6	100	2026	165
1975	7	50	2026	165
1976	8	50	2026	165
1977	9	100	2026	165
1978	10	25	2026	165
1979	11	50	2026	165
1980	12	100	2026	165
1981	1	25	2026	166
1982	2	25	2026	166
1983	3	100	2026	166
1984	4	50	2026	166
1985	5	75	2026	166
1986	6	100	2026	166
1987	7	50	2026	166
1988	8	100	2026	166
1989	9	50	2026	166
1990	10	50	2026	166
1991	11	75	2026	166
1992	12	75	2026	166
1993	1	25	2026	167
1994	2	75	2026	167
1995	3	100	2026	167
1996	4	25	2026	167
1997	5	75	2026	167
1998	6	100	2026	167
1999	7	50	2026	167
2000	8	50	2026	167
2001	9	100	2026	167
2002	10	100	2026	167
2003	11	75	2026	167
2004	12	100	2026	167
2005	1	100	2026	168
2006	2	50	2026	168
2007	3	100	2026	168
2008	4	100	2026	168
2009	5	100	2026	168
2010	6	100	2026	168
2011	7	100	2026	168
2012	8	75	2026	168
2013	9	75	2026	168
2014	10	75	2026	168
2015	11	100	2026	168
2016	12	75	2026	168
2017	1	50	2026	169
2018	2	25	2026	169
2019	3	25	2026	169
2020	4	25	2026	169
2021	5	25	2026	169
2022	6	100	2026	169
2023	7	25	2026	169
2024	8	100	2026	169
2025	9	75	2026	169
2026	10	50	2026	169
2027	11	75	2026	169
2028	12	75	2026	169
2029	1	75	2026	170
2030	2	100	2026	170
2031	3	25	2026	170
2032	4	100	2026	170
2033	5	100	2026	170
2034	6	50	2026	170
2035	7	25	2026	170
2036	8	100	2026	170
2037	9	100	2026	170
2038	10	100	2026	170
2039	11	50	2026	170
2040	12	50	2026	170
2041	1	75	2026	171
2042	2	25	2026	171
2043	3	100	2026	171
2044	4	50	2026	171
2045	5	100	2026	171
2046	6	100	2026	171
2047	7	50	2026	171
2048	8	50	2026	171
2049	9	25	2026	171
2050	10	100	2026	171
2051	11	25	2026	171
2052	12	100	2026	171
2053	1	100	2026	172
2054	2	100	2026	172
2055	3	100	2026	172
2056	4	25	2026	172
2057	5	50	2026	172
2058	6	100	2026	172
2059	7	100	2026	172
2060	8	75	2026	172
2061	9	100	2026	172
2062	10	25	2026	172
2063	11	50	2026	172
2064	12	75	2026	172
2065	1	75	2026	173
2066	2	100	2026	173
2067	3	50	2026	173
2068	4	100	2026	173
2069	5	100	2026	173
2070	6	75	2026	173
2071	7	50	2026	173
2072	8	50	2026	173
2073	9	75	2026	173
2074	10	50	2026	173
2075	11	50	2026	173
2076	12	50	2026	173
2077	1	100	2026	174
2078	2	50	2026	174
2079	3	25	2026	174
2080	4	100	2026	174
2081	5	75	2026	174
2082	6	25	2026	174
2083	7	25	2026	174
2084	8	25	2026	174
2085	9	100	2026	174
2086	10	100	2026	174
2087	11	25	2026	174
2088	12	25	2026	174
2089	1	50	2026	175
2090	2	100	2026	175
2091	3	50	2026	175
2092	4	50	2026	175
2093	5	25	2026	175
2094	6	75	2026	175
2095	7	50	2026	175
2096	8	50	2026	175
2097	9	50	2026	175
2098	10	75	2026	175
2099	11	75	2026	175
2100	12	25	2026	175
2101	1	100	2026	176
2102	2	100	2026	176
2103	3	100	2026	176
2104	4	25	2026	176
2105	5	75	2026	176
2106	6	25	2026	176
2107	7	25	2026	176
2108	8	100	2026	176
2109	9	25	2026	176
2110	10	50	2026	176
2111	11	25	2026	176
2112	12	75	2026	176
2113	1	100	2026	177
2114	2	75	2026	177
2115	3	50	2026	177
2116	4	100	2026	177
2117	5	50	2026	177
2118	6	100	2026	177
2119	7	25	2026	177
2120	8	100	2026	177
2121	9	25	2026	177
2122	10	25	2026	177
2123	11	25	2026	177
2124	12	25	2026	177
2125	1	25	2026	178
2126	2	50	2026	178
2127	3	50	2026	178
2128	4	50	2026	178
2129	5	75	2026	178
2130	6	50	2026	178
2131	7	25	2026	178
2132	8	100	2026	178
2133	9	100	2026	178
2134	10	75	2026	178
2135	11	75	2026	178
2136	12	75	2026	178
2137	1	50	2026	179
2138	2	25	2026	179
2139	3	75	2026	179
2140	4	75	2026	179
2141	5	75	2026	179
2142	6	50	2026	179
2143	7	100	2026	179
2144	8	25	2026	179
2145	9	25	2026	179
2146	10	100	2026	179
2147	11	25	2026	179
2148	12	100	2026	179
2149	1	100	2026	180
2150	2	75	2026	180
2151	3	50	2026	180
2152	4	50	2026	180
2153	5	25	2026	180
2154	6	100	2026	180
2155	7	100	2026	180
2156	8	100	2026	180
2157	9	100	2026	180
2158	10	100	2026	180
2159	11	100	2026	180
2160	12	75	2026	180
2161	1	50	2026	181
2162	2	75	2026	181
2163	3	75	2026	181
2164	4	50	2026	181
2165	5	25	2026	181
2166	6	50	2026	181
2167	7	75	2026	181
2168	8	50	2026	181
2169	9	75	2026	181
2170	10	100	2026	181
2171	11	100	2026	181
2172	12	50	2026	181
2173	1	50	2026	182
2174	2	50	2026	182
2175	3	100	2026	182
2176	4	25	2026	182
2177	5	75	2026	182
2178	6	75	2026	182
2179	7	75	2026	182
2180	8	100	2026	182
2181	9	75	2026	182
2182	10	50	2026	182
2183	11	25	2026	182
2184	12	25	2026	182
2185	1	50	2026	183
2186	2	100	2026	183
2187	3	50	2026	183
2188	4	50	2026	183
2189	5	50	2026	183
2190	6	100	2026	183
2191	7	100	2026	183
2192	8	25	2026	183
2193	9	25	2026	183
2194	10	75	2026	183
2195	11	75	2026	183
2196	12	100	2026	183
2197	1	75	2026	184
2198	2	25	2026	184
2199	3	100	2026	184
2200	4	25	2026	184
2201	5	50	2026	184
2202	6	25	2026	184
2203	7	100	2026	184
2204	8	100	2026	184
2205	9	75	2026	184
2206	10	100	2026	184
2207	11	100	2026	184
2208	12	100	2026	184
2209	1	75	2026	185
2210	2	75	2026	185
2211	3	75	2026	185
2212	4	50	2026	185
2213	5	75	2026	185
2214	6	25	2026	185
2215	7	75	2026	185
2216	8	75	2026	185
2217	9	25	2026	185
2218	10	100	2026	185
2219	11	50	2026	185
2220	12	75	2026	185
2221	1	50	2026	186
2222	2	50	2026	186
2223	3	50	2026	186
2224	4	50	2026	186
2225	5	75	2026	186
2226	6	100	2026	186
2227	7	75	2026	186
2228	8	25	2026	186
2229	9	50	2026	186
2230	10	75	2026	186
2231	11	100	2026	186
2232	12	75	2026	186
2233	1	50	2026	187
2234	2	25	2026	187
2235	3	75	2026	187
2236	4	100	2026	187
2237	5	25	2026	187
2238	6	50	2026	187
2239	7	25	2026	187
2240	8	75	2026	187
2241	9	75	2026	187
2242	10	25	2026	187
2243	11	50	2026	187
2244	12	100	2026	187
2245	1	25	2026	188
2246	2	75	2026	188
2247	3	75	2026	188
2248	4	50	2026	188
2249	5	75	2026	188
2250	6	100	2026	188
2251	7	50	2026	188
2252	8	25	2026	188
2253	9	25	2026	188
2254	10	50	2026	188
2255	11	25	2026	188
2256	12	100	2026	188
2257	1	50	2026	189
2258	2	50	2026	189
2259	3	100	2026	189
2260	4	25	2026	189
2261	5	100	2026	189
2262	6	50	2026	189
2263	7	75	2026	189
2264	8	100	2026	189
2265	9	50	2026	189
2266	10	50	2026	189
2267	11	75	2026	189
2268	12	75	2026	189
2269	1	75	2026	190
2270	2	75	2026	190
2271	3	75	2026	190
2272	4	50	2026	190
2273	5	100	2026	190
2274	6	50	2026	190
2275	7	25	2026	190
2276	8	25	2026	190
2277	9	50	2026	190
2278	10	75	2026	190
2279	11	50	2026	190
2280	12	25	2026	190
2281	1	75	2026	191
2282	2	100	2026	191
2283	3	75	2026	191
2284	4	50	2026	191
2285	5	50	2026	191
2286	6	25	2026	191
2287	7	50	2026	191
2288	8	75	2026	191
2289	9	25	2026	191
2290	10	75	2026	191
2291	11	75	2026	191
2292	12	100	2026	191
2293	1	25	2026	192
2294	2	50	2026	192
2295	3	100	2026	192
2296	4	75	2026	192
2297	5	75	2026	192
2298	6	75	2026	192
2299	7	50	2026	192
2300	8	100	2026	192
2301	9	25	2026	192
2302	10	50	2026	192
2303	11	50	2026	192
2304	12	50	2026	192
2305	1	25	2026	193
2306	2	25	2026	193
2307	3	100	2026	193
2308	4	100	2026	193
2309	5	25	2026	193
2310	6	25	2026	193
2311	7	25	2026	193
2312	8	25	2026	193
2313	9	50	2026	193
2314	10	75	2026	193
2315	11	75	2026	193
2316	12	100	2026	193
2317	1	25	2026	194
2318	2	75	2026	194
2319	3	50	2026	194
2320	4	25	2026	194
2321	5	25	2026	194
2322	6	75	2026	194
2323	7	100	2026	194
2324	8	100	2026	194
2325	9	25	2026	194
2326	10	100	2026	194
2327	11	50	2026	194
2328	12	25	2026	194
2329	1	75	2026	195
2330	2	75	2026	195
2331	3	75	2026	195
2332	4	50	2026	195
2333	5	100	2026	195
2334	6	25	2026	195
2335	7	100	2026	195
2336	8	50	2026	195
2337	9	50	2026	195
2338	10	100	2026	195
2339	11	75	2026	195
2340	12	25	2026	195
2341	1	100	2026	196
2342	2	100	2026	196
2343	3	50	2026	196
2344	4	75	2026	196
2345	5	100	2026	196
2346	6	50	2026	196
2347	7	100	2026	196
2348	8	25	2026	196
2349	9	50	2026	196
2350	10	100	2026	196
2351	11	100	2026	196
2352	12	100	2026	196
2353	1	50	2026	197
2354	2	50	2026	197
2355	3	25	2026	197
2356	4	25	2026	197
2357	5	100	2026	197
2358	6	25	2026	197
2359	7	25	2026	197
2360	8	25	2026	197
2361	9	25	2026	197
2362	10	100	2026	197
2363	11	75	2026	197
2364	12	75	2026	197
2365	1	75	2026	198
2366	2	75	2026	198
2367	3	50	2026	198
2368	4	50	2026	198
2369	5	25	2026	198
2370	6	75	2026	198
2371	7	100	2026	198
2372	8	25	2026	198
2373	9	50	2026	198
2374	10	50	2026	198
2375	11	100	2026	198
2376	12	75	2026	198
2377	1	50	2026	199
2378	2	25	2026	199
2379	3	100	2026	199
2380	4	75	2026	199
2381	5	75	2026	199
2382	6	25	2026	199
2383	7	100	2026	199
2384	8	50	2026	199
2385	9	100	2026	199
2386	10	75	2026	199
2387	11	100	2026	199
2388	12	25	2026	199
2389	1	100	2026	200
2390	2	25	2026	200
2391	3	75	2026	200
2392	4	100	2026	200
2393	5	75	2026	200
2394	6	100	2026	200
2395	7	100	2026	200
2396	8	50	2026	200
2397	9	100	2026	200
2398	10	25	2026	200
2399	11	75	2026	200
2400	12	75	2026	200
2401	1	50	2026	201
2402	2	50	2026	201
2403	3	50	2026	201
2404	4	50	2026	201
2405	5	25	2026	201
2406	6	100	2026	201
2407	7	50	2026	201
2408	8	25	2026	201
2409	9	50	2026	201
2410	10	25	2026	201
2411	11	50	2026	201
2412	12	50	2026	201
2413	1	25	2026	202
2414	2	75	2026	202
2415	3	100	2026	202
2416	4	50	2026	202
2417	5	50	2026	202
2418	6	100	2026	202
2419	7	25	2026	202
2420	8	75	2026	202
2421	9	100	2026	202
2422	10	75	2026	202
2423	11	100	2026	202
2424	12	50	2026	202
2425	1	25	2026	203
2426	2	75	2026	203
2427	3	50	2026	203
2428	4	50	2026	203
2429	5	50	2026	203
2430	6	25	2026	203
2431	7	100	2026	203
2432	8	75	2026	203
2433	9	25	2026	203
2434	10	50	2026	203
2435	11	50	2026	203
2436	12	75	2026	203
2437	1	50	2026	204
2438	2	50	2026	204
2439	3	100	2026	204
2440	4	50	2026	204
2441	5	100	2026	204
2442	6	50	2026	204
2443	7	50	2026	204
2444	8	25	2026	204
2445	9	25	2026	204
2446	10	25	2026	204
2447	11	50	2026	204
2448	12	25	2026	204
2449	1	75	2026	205
2450	2	100	2026	205
2451	3	100	2026	205
2452	4	75	2026	205
2453	5	100	2026	205
2454	6	100	2026	205
2455	7	25	2026	205
2456	8	50	2026	205
2457	9	100	2026	205
2458	10	25	2026	205
2459	11	50	2026	205
2460	12	25	2026	205
2461	1	100	2026	206
2462	2	100	2026	206
2463	3	25	2026	206
2464	4	75	2026	206
2465	5	75	2026	206
2466	6	100	2026	206
2467	7	100	2026	206
2468	8	25	2026	206
2469	9	100	2026	206
2470	10	25	2026	206
2471	11	75	2026	206
2472	12	100	2026	206
2473	1	75	2026	207
2474	2	100	2026	207
2475	3	25	2026	207
2476	4	100	2026	207
2477	5	25	2026	207
2478	6	75	2026	207
2479	7	25	2026	207
2480	8	100	2026	207
2481	9	75	2026	207
2482	10	25	2026	207
2483	11	25	2026	207
2484	12	25	2026	207
2485	1	25	2026	208
2486	2	75	2026	208
2487	3	100	2026	208
2488	4	50	2026	208
2489	5	100	2026	208
2490	6	25	2026	208
2491	7	100	2026	208
2492	8	100	2026	208
2493	9	25	2026	208
2494	10	75	2026	208
2495	11	50	2026	208
2496	12	100	2026	208
2497	1	50	2026	209
2498	2	50	2026	209
2499	3	75	2026	209
2500	4	50	2026	209
2501	5	25	2026	209
2502	6	100	2026	209
2503	7	75	2026	209
2504	8	75	2026	209
2505	9	50	2026	209
2506	10	100	2026	209
2507	11	75	2026	209
2508	12	25	2026	209
2509	1	25	2026	210
2510	2	25	2026	210
2511	3	100	2026	210
2512	4	75	2026	210
2513	5	100	2026	210
2514	6	50	2026	210
2515	7	25	2026	210
2516	8	75	2026	210
2517	9	25	2026	210
2518	10	100	2026	210
2519	11	50	2026	210
2520	12	50	2026	210
2521	1	50	2026	211
2522	2	75	2026	211
2523	3	100	2026	211
2524	4	100	2026	211
2525	5	50	2026	211
2526	6	50	2026	211
2527	7	75	2026	211
2528	8	50	2026	211
2529	9	50	2026	211
2530	10	75	2026	211
2531	11	25	2026	211
2532	12	50	2026	211
2533	1	25	2026	212
2534	2	100	2026	212
2535	3	50	2026	212
2536	4	25	2026	212
2537	5	50	2026	212
2538	6	100	2026	212
2539	7	100	2026	212
2540	8	50	2026	212
2541	9	75	2026	212
2542	10	100	2026	212
2543	11	25	2026	212
2544	12	50	2026	212
2545	1	75	2026	213
2546	2	100	2026	213
2547	3	50	2026	213
2548	4	50	2026	213
2549	5	25	2026	213
2550	6	75	2026	213
2551	7	100	2026	213
2552	8	25	2026	213
2553	9	75	2026	213
2554	10	75	2026	213
2555	11	50	2026	213
2556	12	50	2026	213
2557	1	25	2026	214
2558	2	50	2026	214
2559	3	25	2026	214
2560	4	50	2026	214
2561	5	25	2026	214
2562	6	50	2026	214
2563	7	25	2026	214
2564	8	50	2026	214
2565	9	50	2026	214
2566	10	75	2026	214
2567	11	25	2026	214
2568	12	75	2026	214
2569	1	100	2026	215
2570	2	50	2026	215
2571	3	100	2026	215
2572	4	100	2026	215
2573	5	100	2026	215
2574	6	75	2026	215
2575	7	25	2026	215
2576	8	75	2026	215
2577	9	50	2026	215
2578	10	25	2026	215
2579	11	75	2026	215
2580	12	25	2026	215
2581	1	75	2026	216
2582	2	75	2026	216
2583	3	25	2026	216
2584	4	50	2026	216
2585	5	25	2026	216
2586	6	100	2026	216
2587	7	75	2026	216
2588	8	50	2026	216
2589	9	25	2026	216
2590	10	75	2026	216
2591	11	25	2026	216
2592	12	50	2026	216
2593	1	75	2026	217
2594	2	50	2026	217
2595	3	25	2026	217
2596	4	75	2026	217
2597	5	75	2026	217
2598	6	100	2026	217
2599	7	50	2026	217
2600	8	50	2026	217
2601	9	50	2026	217
2602	10	100	2026	217
2603	11	50	2026	217
2604	12	25	2026	217
2605	1	25	2026	218
2606	2	100	2026	218
2607	3	75	2026	218
2608	4	25	2026	218
2609	5	50	2026	218
2610	6	75	2026	218
2611	7	50	2026	218
2612	8	100	2026	218
2613	9	50	2026	218
2614	10	100	2026	218
2615	11	25	2026	218
2616	12	100	2026	218
2617	1	50	2026	219
2618	2	100	2026	219
2619	3	50	2026	219
2620	4	75	2026	219
2621	5	100	2026	219
2622	6	50	2026	219
2623	7	50	2026	219
2624	8	50	2026	219
2625	9	100	2026	219
2626	10	75	2026	219
2627	11	50	2026	219
2628	12	75	2026	219
2629	1	100	2026	220
2630	2	50	2026	220
2631	3	25	2026	220
2632	4	100	2026	220
2633	5	100	2026	220
2634	6	100	2026	220
2635	7	25	2026	220
2636	8	25	2026	220
2637	9	25	2026	220
2638	10	100	2026	220
2639	11	75	2026	220
2640	12	75	2026	220
2641	1	75	2026	221
2642	2	100	2026	221
2643	3	50	2026	221
2644	4	100	2026	221
2645	5	75	2026	221
2646	6	50	2026	221
2647	7	75	2026	221
2648	8	100	2026	221
2649	9	50	2026	221
2650	10	50	2026	221
2651	11	25	2026	221
2652	12	25	2026	221
2653	1	25	2026	222
2654	2	50	2026	222
2655	3	25	2026	222
2656	4	75	2026	222
2657	5	75	2026	222
2658	6	50	2026	222
2659	7	25	2026	222
2660	8	50	2026	222
2661	9	50	2026	222
2662	10	25	2026	222
2663	11	50	2026	222
2664	12	25	2026	222
2665	1	25	2026	223
2666	2	25	2026	223
2667	3	75	2026	223
2668	4	100	2026	223
2669	5	25	2026	223
2670	6	25	2026	223
2671	7	25	2026	223
2672	8	100	2026	223
2673	9	50	2026	223
2674	10	75	2026	223
2675	11	100	2026	223
2676	12	25	2026	223
2677	1	100	2026	224
2678	2	25	2026	224
2679	3	25	2026	224
2680	4	100	2026	224
2681	5	75	2026	224
2682	6	25	2026	224
2683	7	25	2026	224
2684	8	75	2026	224
2685	9	25	2026	224
2686	10	25	2026	224
2687	11	50	2026	224
2688	12	75	2026	224
2689	1	25	2026	225
2690	2	75	2026	225
2691	3	100	2026	225
2692	4	100	2026	225
2693	5	50	2026	225
2694	6	50	2026	225
2695	7	100	2026	225
2696	8	50	2026	225
2697	9	50	2026	225
2698	10	25	2026	225
2699	11	25	2026	225
2700	12	75	2026	225
2701	1	75	2026	226
2702	2	25	2026	226
2703	3	50	2026	226
2704	4	75	2026	226
2705	5	50	2026	226
2706	6	75	2026	226
2707	7	50	2026	226
2708	8	25	2026	226
2709	9	25	2026	226
2710	10	25	2026	226
2711	11	25	2026	226
2712	12	25	2026	226
2713	1	25	2026	227
2714	2	25	2026	227
2715	3	100	2026	227
2716	4	75	2026	227
2717	5	100	2026	227
2718	6	100	2026	227
2719	7	50	2026	227
2720	8	75	2026	227
2721	9	100	2026	227
2722	10	25	2026	227
2723	11	25	2026	227
2724	12	75	2026	227
2725	1	25	2026	228
2726	2	25	2026	228
2727	3	25	2026	228
2728	4	25	2026	228
2729	5	25	2026	228
2730	6	50	2026	228
2731	7	75	2026	228
2732	8	75	2026	228
2733	9	100	2026	228
2734	10	75	2026	228
2735	11	50	2026	228
2736	12	75	2026	228
2737	1	50	2026	229
2738	2	100	2026	229
2739	3	75	2026	229
2740	4	50	2026	229
2741	5	100	2026	229
2742	6	25	2026	229
2743	7	75	2026	229
2744	8	100	2026	229
2745	9	100	2026	229
2746	10	25	2026	229
2747	11	75	2026	229
2748	12	25	2026	229
2749	1	25	2026	230
2750	2	25	2026	230
2751	3	75	2026	230
2752	4	50	2026	230
2753	5	75	2026	230
2754	6	25	2026	230
2755	7	100	2026	230
2756	8	25	2026	230
2757	9	50	2026	230
2758	10	50	2026	230
2759	11	75	2026	230
2760	12	25	2026	230
2761	1	25	2026	231
2762	2	100	2026	231
2763	3	25	2026	231
2764	4	75	2026	231
2765	5	50	2026	231
2766	6	50	2026	231
2767	7	100	2026	231
2768	8	75	2026	231
2769	9	100	2026	231
2770	10	75	2026	231
2771	11	50	2026	231
2772	12	25	2026	231
2773	1	25	2026	232
2774	2	75	2026	232
2775	3	75	2026	232
2776	4	100	2026	232
2777	5	50	2026	232
2778	6	100	2026	232
2779	7	50	2026	232
2780	8	100	2026	232
2781	9	25	2026	232
2782	10	50	2026	232
2783	11	25	2026	232
2784	12	75	2026	232
2785	1	50	2026	233
2786	2	100	2026	233
2787	3	25	2026	233
2788	4	100	2026	233
2789	5	25	2026	233
2790	6	75	2026	233
2791	7	75	2026	233
2792	8	100	2026	233
2793	9	50	2026	233
2794	10	75	2026	233
2795	11	100	2026	233
2796	12	25	2026	233
2797	1	25	2026	234
2798	2	50	2026	234
2799	3	25	2026	234
2800	4	25	2026	234
2801	5	75	2026	234
2802	6	100	2026	234
2803	7	75	2026	234
2804	8	50	2026	234
2805	9	100	2026	234
2806	10	25	2026	234
2807	11	50	2026	234
2808	12	100	2026	234
2809	1	100	2026	235
2810	2	75	2026	235
2811	3	75	2026	235
2812	4	50	2026	235
2813	5	25	2026	235
2814	6	50	2026	235
2815	7	50	2026	235
2816	8	75	2026	235
2817	9	100	2026	235
2818	10	100	2026	235
2819	11	50	2026	235
2820	12	100	2026	235
2821	1	25	2026	236
2822	2	100	2026	236
2823	3	75	2026	236
2824	4	100	2026	236
2825	5	25	2026	236
2826	6	50	2026	236
2827	7	25	2026	236
2828	8	25	2026	236
2829	9	100	2026	236
2830	10	100	2026	236
2831	11	25	2026	236
2832	12	75	2026	236
2833	1	100	2026	237
2834	2	50	2026	237
2835	3	75	2026	237
2836	4	75	2026	237
2837	5	50	2026	237
2838	6	75	2026	237
2839	7	75	2026	237
2840	8	25	2026	237
2841	9	100	2026	237
2842	10	100	2026	237
2843	11	75	2026	237
2844	12	25	2026	237
2845	1	25	2026	238
2846	2	100	2026	238
2847	3	100	2026	238
2848	4	75	2026	238
2849	5	75	2026	238
2850	6	25	2026	238
2851	7	25	2026	238
2852	8	100	2026	238
2853	9	50	2026	238
2854	10	75	2026	238
2855	11	50	2026	238
2856	12	100	2026	238
2857	1	75	2026	239
2858	2	50	2026	239
2859	3	75	2026	239
2860	4	75	2026	239
2861	5	75	2026	239
2862	6	75	2026	239
2863	7	50	2026	239
2864	8	100	2026	239
2865	9	100	2026	239
2866	10	50	2026	239
2867	11	75	2026	239
2868	12	25	2026	239
2869	1	100	2026	240
2870	2	25	2026	240
2871	3	100	2026	240
2872	4	25	2026	240
2873	5	75	2026	240
2874	6	25	2026	240
2875	7	50	2026	240
2876	8	25	2026	240
2877	9	50	2026	240
2878	10	25	2026	240
2879	11	75	2026	240
2880	12	75	2026	240
2881	1	25	2026	241
2882	2	50	2026	241
2883	3	75	2026	241
2884	4	75	2026	241
2885	5	100	2026	241
2886	6	50	2026	241
2887	7	75	2026	241
2888	8	100	2026	241
2889	9	25	2026	241
2890	10	25	2026	241
2891	11	25	2026	241
2892	12	25	2026	241
2893	1	100	2026	242
2894	2	100	2026	242
2895	3	75	2026	242
2896	4	75	2026	242
2897	5	25	2026	242
2898	6	25	2026	242
2899	7	100	2026	242
2900	8	25	2026	242
2901	9	25	2026	242
2902	10	75	2026	242
2903	11	25	2026	242
2904	12	100	2026	242
2905	1	50	2026	243
2906	2	100	2026	243
2907	3	25	2026	243
2908	4	50	2026	243
2909	5	75	2026	243
2910	6	25	2026	243
2911	7	50	2026	243
2912	8	25	2026	243
2913	9	50	2026	243
2914	10	50	2026	243
2915	11	75	2026	243
2916	12	75	2026	243
2917	1	100	2026	244
2918	2	75	2026	244
2919	3	50	2026	244
2920	4	100	2026	244
2921	5	25	2026	244
2922	6	75	2026	244
2923	7	100	2026	244
2924	8	50	2026	244
2925	9	100	2026	244
2926	10	25	2026	244
2927	11	100	2026	244
2928	12	25	2026	244
2929	1	25	2026	245
2930	2	50	2026	245
2931	3	25	2026	245
2932	4	100	2026	245
2933	5	50	2026	245
2934	6	25	2026	245
2935	7	50	2026	245
2936	8	100	2026	245
2937	9	25	2026	245
2938	10	25	2026	245
2939	11	25	2026	245
2940	12	50	2026	245
2941	1	50	2026	246
2942	2	50	2026	246
2943	3	50	2026	246
2944	4	25	2026	246
2945	5	50	2026	246
2946	6	25	2026	246
2947	7	50	2026	246
2948	8	25	2026	246
2949	9	100	2026	246
2950	10	100	2026	246
2951	11	75	2026	246
2952	12	75	2026	246
2953	1	100	2026	247
2954	2	100	2026	247
2955	3	100	2026	247
2956	4	75	2026	247
2957	5	50	2026	247
2958	6	25	2026	247
2959	7	75	2026	247
2960	8	100	2026	247
2961	9	25	2026	247
2962	10	25	2026	247
2963	11	75	2026	247
2964	12	75	2026	247
2965	1	100	2026	248
2966	2	50	2026	248
2967	3	75	2026	248
2968	4	50	2026	248
2969	5	100	2026	248
2970	6	100	2026	248
2971	7	75	2026	248
2972	8	75	2026	248
2973	9	50	2026	248
2974	10	50	2026	248
2975	11	75	2026	248
2976	12	75	2026	248
2977	1	50	2026	249
2978	2	75	2026	249
2979	3	25	2026	249
2980	4	75	2026	249
2981	5	25	2026	249
2982	6	25	2026	249
2983	7	25	2026	249
2984	8	100	2026	249
2985	9	50	2026	249
2986	10	100	2026	249
2987	11	25	2026	249
2988	12	50	2026	249
2989	1	50	2026	250
2990	2	100	2026	250
2991	3	50	2026	250
2992	4	75	2026	250
2993	5	75	2026	250
2994	6	25	2026	250
2995	7	25	2026	250
2996	8	100	2026	250
2997	9	50	2026	250
2998	10	50	2026	250
2999	11	25	2026	250
3000	12	25	2026	250
3001	1	50	2026	251
3002	2	100	2026	251
3003	3	25	2026	251
3004	4	75	2026	251
3005	5	25	2026	251
3006	6	75	2026	251
3007	7	50	2026	251
3008	8	25	2026	251
3009	9	75	2026	251
3010	10	25	2026	251
3011	11	75	2026	251
3012	12	75	2026	251
3013	1	25	2026	252
3014	2	50	2026	252
3015	3	100	2026	252
3016	4	100	2026	252
3017	5	75	2026	252
3018	6	50	2026	252
3019	7	25	2026	252
3020	8	75	2026	252
3021	9	50	2026	252
3022	10	25	2026	252
3023	11	100	2026	252
3024	12	100	2026	252
3025	1	25	2026	253
3026	2	75	2026	253
3027	3	25	2026	253
3028	4	75	2026	253
3029	5	75	2026	253
3030	6	75	2026	253
3031	7	50	2026	253
3032	8	25	2026	253
3033	9	50	2026	253
3034	10	25	2026	253
3035	11	25	2026	253
3036	12	75	2026	253
3037	1	50	2026	254
3038	2	100	2026	254
3039	3	25	2026	254
3040	4	50	2026	254
3041	5	50	2026	254
3042	6	75	2026	254
3043	7	25	2026	254
3044	8	25	2026	254
3045	9	75	2026	254
3046	10	25	2026	254
3047	11	100	2026	254
3048	12	100	2026	254
3049	1	100	2026	255
3050	2	75	2026	255
3051	3	100	2026	255
3052	4	25	2026	255
3053	5	100	2026	255
3054	6	75	2026	255
3055	7	50	2026	255
3056	8	50	2026	255
3057	9	50	2026	255
3058	10	25	2026	255
3059	11	25	2026	255
3060	12	75	2026	255
3061	1	50	2026	256
3062	2	100	2026	256
3063	3	75	2026	256
3064	4	100	2026	256
3065	5	25	2026	256
3066	6	75	2026	256
3067	7	75	2026	256
3068	8	25	2026	256
3069	9	50	2026	256
3070	10	75	2026	256
3071	11	100	2026	256
3072	12	100	2026	256
3073	1	75	2026	257
3074	2	25	2026	257
3075	3	50	2026	257
3076	4	75	2026	257
3077	5	100	2026	257
3078	6	75	2026	257
3079	7	100	2026	257
3080	8	50	2026	257
3081	9	50	2026	257
3082	10	100	2026	257
3083	11	100	2026	257
3084	12	25	2026	257
3085	1	50	2026	258
3086	2	100	2026	258
3087	3	50	2026	258
3088	4	50	2026	258
3089	5	75	2026	258
3090	6	50	2026	258
3091	7	75	2026	258
3092	8	100	2026	258
3093	9	25	2026	258
3094	10	50	2026	258
3095	11	25	2026	258
3096	12	50	2026	258
3097	1	75	2026	259
3098	2	100	2026	259
3099	3	50	2026	259
3100	4	75	2026	259
3101	5	75	2026	259
3102	6	50	2026	259
3103	7	75	2026	259
3104	8	100	2026	259
3105	9	75	2026	259
3106	10	50	2026	259
3107	11	25	2026	259
3108	12	100	2026	259
3109	1	100	2026	260
3110	2	50	2026	260
3111	3	50	2026	260
3112	4	75	2026	260
3113	5	75	2026	260
3114	6	25	2026	260
3115	7	50	2026	260
3116	8	75	2026	260
3117	9	100	2026	260
3118	10	25	2026	260
3119	11	100	2026	260
3120	12	75	2026	260
3121	1	25	2026	261
3122	2	100	2026	261
3123	3	25	2026	261
3124	4	25	2026	261
3125	5	75	2026	261
3126	6	100	2026	261
3127	7	50	2026	261
3128	8	50	2026	261
3129	9	50	2026	261
3130	10	75	2026	261
3131	11	25	2026	261
3132	12	50	2026	261
3133	1	25	2026	262
3134	2	25	2026	262
3135	3	25	2026	262
3136	4	100	2026	262
3137	5	25	2026	262
3138	6	100	2026	262
3139	7	100	2026	262
3140	8	100	2026	262
3141	9	75	2026	262
3142	10	25	2026	262
3143	11	25	2026	262
3144	12	75	2026	262
3145	1	25	2026	263
3146	2	100	2026	263
3147	3	75	2026	263
3148	4	25	2026	263
3149	5	50	2026	263
3150	6	25	2026	263
3151	7	50	2026	263
3152	8	100	2026	263
3153	9	25	2026	263
3154	10	100	2026	263
3155	11	25	2026	263
3156	12	25	2026	263
3157	1	100	2026	264
3158	2	50	2026	264
3159	3	25	2026	264
3160	4	25	2026	264
3161	5	75	2026	264
3162	6	50	2026	264
3163	7	50	2026	264
3164	8	50	2026	264
3165	9	50	2026	264
3166	10	25	2026	264
3167	11	25	2026	264
3168	12	100	2026	264
3169	1	25	2026	265
3170	2	25	2026	265
3171	3	50	2026	265
3172	4	50	2026	265
3173	5	25	2026	265
3174	6	50	2026	265
3175	7	100	2026	265
3176	8	100	2026	265
3177	9	50	2026	265
3178	10	25	2026	265
3179	11	50	2026	265
3180	12	50	2026	265
3181	1	75	2026	266
3182	2	100	2026	266
3183	3	100	2026	266
3184	4	25	2026	266
3185	5	50	2026	266
3186	6	25	2026	266
3187	7	100	2026	266
3188	8	100	2026	266
3189	9	25	2026	266
3190	10	75	2026	266
3191	11	100	2026	266
3192	12	100	2026	266
3193	1	25	2026	267
3194	2	25	2026	267
3195	3	25	2026	267
3196	4	50	2026	267
3197	5	100	2026	267
3198	6	25	2026	267
3199	7	100	2026	267
3200	8	100	2026	267
3201	9	25	2026	267
3202	10	25	2026	267
3203	11	25	2026	267
3204	12	100	2026	267
3205	1	50	2026	268
3206	2	75	2026	268
3207	3	75	2026	268
3208	4	25	2026	268
3209	5	75	2026	268
3210	6	25	2026	268
3211	7	50	2026	268
3212	8	50	2026	268
3213	9	50	2026	268
3214	10	75	2026	268
3215	11	50	2026	268
3216	12	50	2026	268
3217	1	75	2026	269
3218	2	50	2026	269
3219	3	75	2026	269
3220	4	100	2026	269
3221	5	50	2026	269
3222	6	75	2026	269
3223	7	25	2026	269
3224	8	75	2026	269
3225	9	25	2026	269
3226	10	75	2026	269
3227	11	75	2026	269
3228	12	75	2026	269
3229	1	75	2026	270
3230	2	25	2026	270
3231	3	75	2026	270
3232	4	50	2026	270
3233	5	50	2026	270
3234	6	100	2026	270
3235	7	25	2026	270
3236	8	100	2026	270
3237	9	75	2026	270
3238	10	25	2026	270
3239	11	50	2026	270
3240	12	25	2026	270
3241	1	75	2026	271
3242	2	25	2026	271
3243	3	75	2026	271
3244	4	75	2026	271
3245	5	75	2026	271
3246	6	25	2026	271
3247	7	100	2026	271
3248	8	75	2026	271
3249	9	75	2026	271
3250	10	25	2026	271
3251	11	100	2026	271
3252	12	100	2026	271
3253	1	75	2026	272
3254	2	100	2026	272
3255	3	50	2026	272
3256	4	25	2026	272
3257	5	25	2026	272
3258	6	75	2026	272
3259	7	100	2026	272
3260	8	50	2026	272
3261	9	50	2026	272
3262	10	50	2026	272
3263	11	100	2026	272
3264	12	75	2026	272
3265	1	100	2026	273
3266	2	25	2026	273
3267	3	75	2026	273
3268	4	25	2026	273
3269	5	100	2026	273
3270	6	75	2026	273
3271	7	25	2026	273
3272	8	50	2026	273
3273	9	50	2026	273
3274	10	50	2026	273
3275	11	75	2026	273
3276	12	25	2026	273
3277	1	100	2026	274
3278	2	100	2026	274
3279	3	100	2026	274
3280	4	75	2026	274
3281	5	50	2026	274
3282	6	25	2026	274
3283	7	25	2026	274
3284	8	25	2026	274
3285	9	50	2026	274
3286	10	100	2026	274
3287	11	25	2026	274
3288	12	50	2026	274
3289	1	75	2026	275
3290	2	50	2026	275
3291	3	100	2026	275
3292	4	25	2026	275
3293	5	75	2026	275
3294	6	25	2026	275
3295	7	75	2026	275
3296	8	25	2026	275
3297	9	75	2026	275
3298	10	50	2026	275
3299	11	50	2026	275
3300	12	25	2026	275
3301	1	25	2026	276
3302	2	100	2026	276
3303	3	100	2026	276
3304	4	75	2026	276
3305	5	75	2026	276
3306	6	25	2026	276
3307	7	25	2026	276
3308	8	25	2026	276
3309	9	25	2026	276
3310	10	50	2026	276
3311	11	25	2026	276
3312	12	25	2026	276
3313	1	75	2026	277
3314	2	25	2026	277
3315	3	50	2026	277
3316	4	100	2026	277
3317	5	50	2026	277
3318	6	50	2026	277
3319	7	25	2026	277
3320	8	75	2026	277
3321	9	100	2026	277
3322	10	25	2026	277
3323	11	50	2026	277
3324	12	25	2026	277
3325	1	75	2026	278
3326	2	100	2026	278
3327	3	25	2026	278
3328	4	100	2026	278
3329	5	75	2026	278
3330	6	50	2026	278
3331	7	100	2026	278
3332	8	100	2026	278
3333	9	25	2026	278
3334	10	25	2026	278
3335	11	50	2026	278
3336	12	25	2026	278
\.


--
-- Data for Name: projects; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.projects (id, description, end_date, project_id, project_type, region, start_date, status, vertical, manager_id) FROM stdin;
12	ML Pipeline	2026-11-11	PRJ-1011	OPPORTUNITY	Asia	2025-09-11	ON_HOLD	Banking	11
3	Infrastructure Upgrade	2026-11-11	PRJ-1002	PROJECT	MEA	2026-02-11	COMPLETED	Energy	6
9	Data Lake	2026-09-11	PRJ-1008	OPPORTUNITY	Asia	2025-06-11	ON_HOLD	Banking	10
4	Cybersecurity Platform	2026-04-11	PRJ-1003	OPPORTUNITY	MEA	2025-07-11	COMPLETED	Energy	7
14	IoT Platform	2026-09-11	PRJ-1013	PROJECT	Americas	2026-02-11	ON_HOLD	Healthcare	13
16	Industrial Automation	2026-04-11	PRJ-1015	OPPORTUNITY	Americas	2025-08-11	COMPLETED	Healthcare	13
6	Mobile App	2026-04-11	PRJ-1005	PROJECT	Europe	2025-07-11	ON_HOLD	Telecom	9
15	SCADA Modernization	2026-10-11	PRJ-1014	PROJECT	Americas	2025-08-11	ON_HOLD	Healthcare	12
2	Network Security	2026-08-11	PRJ-1001	PROJECT	MEA	2025-07-11	COMPLETED	Energy	7
1	Cloud Migration	2026-09-11	PRJ-1000	OPPORTUNITY	MEA	2025-10-11	COMPLETED	Energy	6
7	Customer Portal	2027-01-11	PRJ-1006	PROJECT	Europe	2025-05-11	ON_HOLD	Telecom	8
10	AI Platform	2026-11-11	PRJ-1009	PROJECT	Asia	2025-10-11	COMPLETED	Banking	11
13	Smart Meters	2026-03-11	PRJ-1012	OPPORTUNITY	Americas	2026-02-11	ON_HOLD	Healthcare	12
8	ERP Integration	2026-10-11	PRJ-1007	OPPORTUNITY	Europe	2025-12-11	ON_HOLD	Telecom	9
5	Digital Transformation	2026-05-11	PRJ-1004	OPPORTUNITY	Europe	2025-07-11	COMPLETED	Telecom	8
11	Analytics Dashboard	2026-11-11	PRJ-1010	PROJECT	Asia	2025-04-11	COMPLETED	Banking	10
\.


--
-- Data for Name: skills; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.skills (id, description, tower_id) FROM stdin;
1	AWS	2
2	Azure	2
3	GCP	2
4	Docker	2
5	Kubernetes	2
6	Terraform	2
7	Linux	2
8	Cisco Networking	3
9	Firewall Management	3
10	SIEM	3
11	Penetration Testing	3
12	IAM	3
13	Endpoint Security	3
14	Selenium	5
15	JUnit	5
16	Cypress	5
17	Performance Testing	5
18	API Testing	5
19	Test Automation	5
20	Java	6
21	Spring Boot	6
22	Angular	6
23	React	6
24	Python	6
25	Node.js	6
26	TypeScript	6
27	ISTQB	7
28	Test Planning	7
29	Regression Testing	7
30	UAT	7
31	BDD	7
32	Defect Management	7
33	Scrum	9
34	SAFe	9
35	Kanban	9
36	Jira	9
37	Agile Coaching	9
38	Product Ownership	9
39	Spark	10
40	Kafka	10
41	Airflow	10
42	Snowflake	10
43	dbt	10
44	SQL	10
45	ETL	10
46	Power BI	11
47	Tableau	11
48	Python Analytics	11
49	R	11
50	Machine Learning	11
51	Statistics	11
52	PLC Programming	13
53	SCADA	13
54	DCS	13
55	HMI Design	13
56	Industrial IoT	13
57	Modbus	13
58	MES	14
59	Historian	14
60	OPC UA	14
61	Safety Systems	14
62	Control Valves	14
63	Instrumentation	14
\.


--
-- Data for Name: tech_towers; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.tech_towers (id, description, parent_tower_id) FROM stdin;
1	EPIS	\N
2	Cloud & Core Infrastructure Services	1
3	Network, Cybersecurity & Collaboration	1
4	Application	\N
5	Testing	4
6	Development	4
7	Quality Assurance	4
8	Data&Agility	\N
9	Agility	8
10	Data Engineering	8
11	Analytics	8
12	OT	\N
13	Automation and Control	12
14	Industrial Systems	12
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.users (id, email, password, username, employee_id) FROM stdin;
1	ahmed.el-sayed130@company.com	$2a$10$vQHhE3piMtxUN60SSaJ6.uZKcMzvRmo/NkTdyFQcP/5EtpjyOX8dS	ahmed.el-sayed	1
2	mohamed.hassan884@company.com	$2a$10$k/XmkYd48S5NVSVxXlIFL.V6yShAUVreOHbxDC8mc8KZ3USgbbWc6	mohamed.hassan	2
3	sara.ibrahim505@company.com	$2a$10$P/qNWtlxFOhbBtAhY37NdOHtEYkbXQoFWFSSJyw33Yv2FR8TloDY6	sara.ibrahim	3
4	khaled.nasser93@company.com	$2a$10$Cn7O31in7ox9YYa7mHGYR.xZzPWCZiquAdKC8RPM4R1PBD/bFU.cW	khaled.nasser	4
5	hassan.farouk276@company.com	$2a$10$3v6fZeo48Q2XG9zYSuanEOSYQdJjrOEmi8GJn3oi7RgdlQrUEZrXq	hassan.farouk	5
6	omar.mohamed32@company.com	$2a$10$xv.gtJoilJVeC.pPPPO3bO69M9L/84PANrZyW2S.DVneiEpL/xJx2	omar.mohamed	6
7	ali.mahmoud743@company.com	$2a$10$Acdd7tqwdfGyInGDTTrV8ODMOu75ek566HXH9PW4yOlhz0mmuV.bm	ali.mahmoud	7
8	marwa.ali863@company.com	$2a$10$0ZJ3Zk/YTIoSmTta3ScnD.46.98uLK/uW/vhgykH9dWqMIm6CxM3i	marwa.ali	8
9	dina.salem243@company.com	$2a$10$5qL9sem.56lTMiaYXJX77O89ooZcPD40bisIIik3m3j9neH1VtrlW	dina.salem	9
10	youssef.kamal458@company.com	$2a$10$AiDr/Lx9UGDLugI/y948tO4a0IjLDGqWSXdZRNZ/ADcAdnyPwYrWa	youssef.kamal	10
11	reem.rashad730@company.com	$2a$10$R77gC76HMIZKL8vOjt7kpeEJCMedBo01zPsqXCwtI6m9FUTfmKaae	reem.rashad	11
12	mahmoud.ibrahim85@company.com	$2a$10$BnBNz/jCBvRWFH/9YscK1OTDeYPVgGproGceC4pYfCIby8ugDZHLe	mahmoud.ibrahim	12
13	layla.hassan212@company.com	$2a$10$kKbYKTHdNkkeJO8HtVtemecYry8IH/T1piyWEsW6nuDFIyuj2YqBC	layla.hassan	13
14	nour.mostafa164@company.com	$2a$10$OtX7WEvjO.RUu5T33nz8dOkS4EMxdUDsp9L2N8HuDr7VVah.Hv3XC	nour.mostafa	14
15	fatma.ahmed823@company.com	$2a$10$rlF16zL0YJ8wQE9rh79CluP9V7n1VGWFO9bDvWeVyC3ddRbUiasMS	fatma.ahmed	15
16	hana.mohamed534@company.com	$2a$10$2LXlmTrovtDL.Rfwhso/QOFiGFYiKAhnGhZH.F7G6OYo8HJ/GsLaG	hana.mohamed	16
17	aisha.ali157@company.com	$2a$10$vLenpEcXDWB54PllGy272OLutYBnUn5UpAeC04rnvUN7J7kakEpya	aisha.ali	17
18	mona.hassan440@company.com	$2a$10$YjP9D.xP32wN0Jx19k5h..fZrYaTFoU.YjvO52WvRAzwZ9WL5qZ.C	mona.hassan	18
19	ahmed.abdel160@company.com	$2a$10$4NuB6BQ28Yep4zFBTvwlt.ZLGnMteKN8fhnaGlKaf8TERxL5CDBmO	ahmed.abdel	19
20	mohamed.kamal59@company.com	$2a$10$FAxpRHbgz2xeE4kfdZMy4OFz21mgx6u4HLYwI0r17ljQBXAl3Agoa	mohamed.kamal	20
21	omar.salem897@company.com	$2a$10$rOEV1XNGWBz6ymp5eADcb.q/032wUuYbxHMnPkt6yy7BkzTfcQ.v6	omar.salem	21
\.


--
-- Name: allocations_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.allocations_id_seq', 278, true);


--
-- Name: employees_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.employees_id_seq', 211, true);


--
-- Name: employees_skills_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.employees_skills_id_seq', 400, true);


--
-- Name: monthly_allocations_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.monthly_allocations_id_seq', 3336, true);


--
-- Name: projects_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.projects_id_seq', 16, true);


--
-- Name: skills_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.skills_id_seq', 63, true);


--
-- Name: tech_towers_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.tech_towers_id_seq', 14, true);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.users_id_seq', 21, true);


--
-- Name: allocations allocations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.allocations
    ADD CONSTRAINT allocations_pkey PRIMARY KEY (id);


--
-- Name: employees employees_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT employees_pkey PRIMARY KEY (id);


--
-- Name: employees_skills employees_skills_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employees_skills
    ADD CONSTRAINT employees_skills_pkey PRIMARY KEY (id);


--
-- Name: monthly_allocations monthly_allocations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.monthly_allocations
    ADD CONSTRAINT monthly_allocations_pkey PRIMARY KEY (id);


--
-- Name: projects projects_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT projects_pkey PRIMARY KEY (id);


--
-- Name: skills skills_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.skills
    ADD CONSTRAINT skills_pkey PRIMARY KEY (id);


--
-- Name: tech_towers tech_towers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tech_towers
    ADD CONSTRAINT tech_towers_pkey PRIMARY KEY (id);


--
-- Name: users uk_6dotkott2kjsp8vw4d0m25fb7; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);


--
-- Name: employees uk_7046dnihcwphbs2tcujqqs5b7; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT uk_7046dnihcwphbs2tcujqqs5b7 UNIQUE (oracle_id);


--
-- Name: users uk_d1s31g1a7ilra77m65xmka3ei; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_d1s31g1a7ilra77m65xmka3ei UNIQUE (employee_id);


--
-- Name: users uk_r43af9ap4edm43mmtq01oddj6; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_r43af9ap4edm43mmtq01oddj6 UNIQUE (username);


--
-- Name: projects uk_r4sng6mj7mni8wyteu3g52kbr; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT uk_r4sng6mj7mni8wyteu3g52kbr UNIQUE (project_id);


--
-- Name: monthly_allocations ukjlp4c7ch2253194diftkj5jj1; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.monthly_allocations
    ADD CONSTRAINT ukjlp4c7ch2253194diftkj5jj1 UNIQUE (allocation_id, year, month);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: users fk6p2ib82uai0pj9yk1iassppgq; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fk6p2ib82uai0pj9yk1iassppgq FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: skills fka03tser7qt8gnjfn5jjse2fmt; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.skills
    ADD CONSTRAINT fka03tser7qt8gnjfn5jjse2fmt FOREIGN KEY (tower_id) REFERENCES public.tech_towers(id);


--
-- Name: employees fkeprn95gifa3sdcxevnwuhu5iq; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT fkeprn95gifa3sdcxevnwuhu5iq FOREIGN KEY (tower) REFERENCES public.tech_towers(id);


--
-- Name: tech_towers fkfugdwgptxb30h2mknrcr1p5c6; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tech_towers
    ADD CONSTRAINT fkfugdwgptxb30h2mknrcr1p5c6 FOREIGN KEY (parent_tower_id) REFERENCES public.tech_towers(id);


--
-- Name: employees fki4365uo9af35g7jtbc2rteukt; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT fki4365uo9af35g7jtbc2rteukt FOREIGN KEY (manager_id) REFERENCES public.employees(id);


--
-- Name: allocations fklgyvi1ifhphug0govaqwnsqyf; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.allocations
    ADD CONSTRAINT fklgyvi1ifhphug0govaqwnsqyf FOREIGN KEY (project_id) REFERENCES public.projects(id);


--
-- Name: allocations fklidedpimhddtisgg66kca1to7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.allocations
    ADD CONSTRAINT fklidedpimhddtisgg66kca1to7 FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: employees_skills fklvgjy8cqpr0velqri2bdboe10; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employees_skills
    ADD CONSTRAINT fklvgjy8cqpr0velqri2bdboe10 FOREIGN KEY (skill_id) REFERENCES public.skills(id);


--
-- Name: employees_skills fknx4hqen57f683awnow675mgb6; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employees_skills
    ADD CONSTRAINT fknx4hqen57f683awnow675mgb6 FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: projects fksg57tut2cx77vmci14sy4vbsu; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT fksg57tut2cx77vmci14sy4vbsu FOREIGN KEY (manager_id) REFERENCES public.employees(id);


--
-- Name: monthly_allocations fkswo94fmi0ls6qher5bvdawry8; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.monthly_allocations
    ADD CONSTRAINT fkswo94fmi0ls6qher5bvdawry8 FOREIGN KEY (allocation_id) REFERENCES public.allocations(id);


--
-- PostgreSQL database dump complete
--

\unrestrict 9SsoNr18iCh53m7cipdmLc8QbPq8WzAt7gcJCS1Tg7CPwyqfbt2Z94vSL1xHSuq

