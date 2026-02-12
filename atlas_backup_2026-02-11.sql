--
-- PostgreSQL database dump
--

\restrict m5QX1iBaLigryipKQFhEixUg7jhSKfYv8Yh6iINDB4FFw9QPRex0O8d3HPX3Lmo

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

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: allocations; Type: TABLE; Schema: public; Owner: atlas
--

CREATE TABLE public.allocations (
    id bigint NOT NULL,
    end_date date,
    start_date date,
    status character varying(255),
    employee_id bigint NOT NULL,
    project_id bigint NOT NULL,
    CONSTRAINT allocations_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'PROSPECT'::character varying])::text[])))
);


ALTER TABLE public.allocations OWNER TO atlas;

--
-- Name: allocations_id_seq; Type: SEQUENCE; Schema: public; Owner: atlas
--

CREATE SEQUENCE public.allocations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.allocations_id_seq OWNER TO atlas;

--
-- Name: allocations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: atlas
--

ALTER SEQUENCE public.allocations_id_seq OWNED BY public.allocations.id;


--
-- Name: employees; Type: TABLE; Schema: public; Owner: atlas
--

CREATE TABLE public.employees (
    id bigint NOT NULL,
    cost_center character varying(255),
    email character varying(255),
    future_manager character varying(255),
    gender character varying(255),
    grade character varying(255) NOT NULL,
    hire_date date,
    hiring_type character varying(255),
    is_active boolean,
    job_level character varying(255),
    legal_entity character varying(255),
    location character varying(255),
    name character varying(255) NOT NULL,
    nationality character varying(255),
    oracle_id character varying(255),
    parent_tower character varying(255),
    primary_skill character varying(255),
    reason_of_leave character varying(255),
    resignation_date date,
    secondary_skill character varying(255),
    status character varying(255),
    title character varying(255),
    tower character varying(255),
    manager_id bigint,
    CONSTRAINT employees_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'MATERNITY'::character varying, 'LONG_LEAVE'::character varying, 'RESIGNED'::character varying])::text[])))
);


ALTER TABLE public.employees OWNER TO atlas;

--
-- Name: employees_id_seq; Type: SEQUENCE; Schema: public; Owner: atlas
--

CREATE SEQUENCE public.employees_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.employees_id_seq OWNER TO atlas;

--
-- Name: employees_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: atlas
--

ALTER SEQUENCE public.employees_id_seq OWNED BY public.employees.id;


--
-- Name: monthly_allocations; Type: TABLE; Schema: public; Owner: atlas
--

CREATE TABLE public.monthly_allocations (
    id bigint NOT NULL,
    allocation_id bigint NOT NULL,
    year integer NOT NULL,
    month integer NOT NULL,
    percentage double precision NOT NULL
);


ALTER TABLE public.monthly_allocations OWNER TO atlas;

--
-- Name: monthly_allocations_id_seq; Type: SEQUENCE; Schema: public; Owner: atlas
--

CREATE SEQUENCE public.monthly_allocations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.monthly_allocations_id_seq OWNER TO atlas;

--
-- Name: monthly_allocations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: atlas
--

ALTER SEQUENCE public.monthly_allocations_id_seq OWNED BY public.monthly_allocations.id;


--
-- Name: projects; Type: TABLE; Schema: public; Owner: atlas
--

CREATE TABLE public.projects (
    id bigint NOT NULL,
    description character varying(255),
    end_date date,
    name character varying(255) NOT NULL,
    parent_tower character varying(255),
    project_id character varying(255) NOT NULL,
    start_date date,
    status character varying(255),
    tower character varying(255),
    manager_id bigint,
    CONSTRAINT projects_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'COMPLETED'::character varying, 'ON_HOLD'::character varying])::text[])))
);


ALTER TABLE public.projects OWNER TO atlas;

--
-- Name: projects_id_seq; Type: SEQUENCE; Schema: public; Owner: atlas
--

CREATE SEQUENCE public.projects_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.projects_id_seq OWNER TO atlas;

--
-- Name: projects_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: atlas
--

ALTER SEQUENCE public.projects_id_seq OWNED BY public.projects.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: atlas
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    email character varying(255) NOT NULL,
    is_active boolean,
    manager_level integer,
    password character varying(255) NOT NULL,
    role character varying(255) NOT NULL,
    username character varying(255) NOT NULL,
    employee_id bigint,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['SYSTEM_ADMIN'::character varying, 'EXECUTIVE'::character varying, 'HEAD'::character varying, 'DEPARTMENT_MANAGER'::character varying, 'TEAM_LEAD'::character varying])::text[])))
);


ALTER TABLE public.users OWNER TO atlas;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: atlas
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.users_id_seq OWNER TO atlas;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: atlas
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: allocations id; Type: DEFAULT; Schema: public; Owner: atlas
--

ALTER TABLE ONLY public.allocations ALTER COLUMN id SET DEFAULT nextval('public.allocations_id_seq'::regclass);


--
-- Name: employees id; Type: DEFAULT; Schema: public; Owner: atlas
--

ALTER TABLE ONLY public.employees ALTER COLUMN id SET DEFAULT nextval('public.employees_id_seq'::regclass);


--
-- Name: monthly_allocations id; Type: DEFAULT; Schema: public; Owner: atlas
--

ALTER TABLE ONLY public.monthly_allocations ALTER COLUMN id SET DEFAULT nextval('public.monthly_allocations_id_seq'::regclass);


--
-- Name: projects id; Type: DEFAULT; Schema: public; Owner: atlas
--

ALTER TABLE ONLY public.projects ALTER COLUMN id SET DEFAULT nextval('public.projects_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: atlas
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Data for Name: allocations; Type: TABLE DATA; Schema: public; Owner: atlas
--

COPY public.allocations (id, end_date, start_date, status, employee_id, project_id) FROM stdin;
9	2026-04-08	2026-02-08	ACTIVE	27	7
11	2026-02-08	2025-09-08	ACTIVE	29	16
13	2026-02-08	2025-11-08	ACTIVE	31	7
16	2026-04-08	2025-11-08	ACTIVE	34	15
19	2026-03-08	2025-12-08	ACTIVE	36	14
23	2026-05-08	2026-02-08	ACTIVE	39	11
27	2026-06-08	2025-11-08	ACTIVE	43	14
28	2026-04-08	2026-01-08	ACTIVE	43	4
32	2026-04-08	2026-02-08	ACTIVE	47	7
33	2026-06-08	2026-02-08	ACTIVE	48	2
38	2026-07-08	2025-10-08	ACTIVE	52	2
39	2026-04-08	2025-09-08	ACTIVE	52	7
45	2026-07-08	2025-12-08	ACTIVE	56	4
46	2026-04-08	2025-12-08	ACTIVE	57	9
48	2026-03-08	2025-09-08	ACTIVE	58	4
53	2026-06-08	2025-12-08	ACTIVE	61	4
54	2026-03-08	2025-12-08	ACTIVE	62	1
56	2026-03-08	2025-11-08	ACTIVE	63	6
8	2026-03-08	2025-10-08	PROSPECT	26	8
15	2026-06-08	2025-12-08	PROSPECT	33	4
22	2026-04-08	2025-12-08	PROSPECT	38	2
29	2026-07-08	2026-02-08	PROSPECT	44	14
36	2026-03-08	2025-10-08	PROSPECT	50	14
43	2026-07-08	2025-09-08	PROSPECT	55	12
50	2026-02-08	2025-09-08	PROSPECT	59	3
2	2026-06-08	2025-09-08	PROSPECT	23	14
10	2026-04-08	2026-01-08	PROSPECT	28	11
18	2026-06-08	2025-11-08	PROSPECT	35	5
26	2026-07-08	2025-12-08	PROSPECT	42	7
34	2026-02-08	2025-10-08	PROSPECT	48	14
42	2026-07-08	2025-09-08	PROSPECT	54	14
51	2026-04-08	2025-12-08	PROSPECT	60	4
3	2026-03-08	2026-01-08	PROSPECT	23	10
12	2026-07-08	2025-09-08	PROSPECT	30	15
21	2026-03-08	2026-02-08	PROSPECT	38	13
31	2026-03-08	2025-12-08	PROSPECT	46	10
40	2026-04-08	2026-02-08	PROSPECT	53	12
49	2026-03-08	2026-01-08	PROSPECT	59	10
4	2026-07-08	2025-11-08	PROSPECT	24	2
14	2026-02-08	2025-11-08	PROSPECT	32	2
25	2026-04-08	2025-10-08	PROSPECT	41	10
37	2026-02-08	2025-12-08	PROSPECT	51	13
47	2026-06-08	2026-01-08	PROSPECT	57	12
5	2026-07-08	2025-12-08	PROSPECT	24	11
17	2026-05-08	2025-09-08	PROSPECT	34	10
30	2026-05-08	2025-10-08	PROSPECT	45	10
44	2026-05-08	2025-12-08	PROSPECT	56	1
55	2026-04-08	2026-01-08	PROSPECT	63	9
6	2026-02-08	2026-01-08	PROSPECT	25	10
20	2026-06-08	2025-10-08	PROSPECT	37	6
35	2026-07-08	2026-01-08	PROSPECT	49	7
52	2026-07-08	2026-01-08	PROSPECT	61	9
7	2026-05-08	2026-01-08	PROSPECT	26	15
24	2026-04-08	2025-10-08	PROSPECT	40	10
41	2026-06-08	2025-12-08	PROSPECT	54	5
58	2026-04-08	2025-12-08	ACTIVE	64	3
66	2026-04-08	2025-09-08	ACTIVE	70	16
68	2026-07-08	2025-11-08	ACTIVE	71	15
70	2026-02-08	2026-01-08	ACTIVE	72	12
73	2026-03-08	2025-10-08	ACTIVE	74	1
76	2026-07-08	2025-12-08	ACTIVE	76	5
80	2026-05-08	2026-02-08	ACTIVE	79	6
84	2026-05-08	2026-02-08	ACTIVE	81	6
86	2026-04-08	2025-11-08	ACTIVE	82	4
89	2026-04-08	2025-09-08	ACTIVE	84	9
90	2026-04-08	2026-01-08	ACTIVE	85	2
95	2026-02-08	2025-12-08	ACTIVE	89	5
96	2026-07-08	2025-11-08	ACTIVE	89	9
102	2026-02-08	2026-02-08	ACTIVE	93	5
103	2026-05-08	2025-12-08	ACTIVE	94	8
105	2026-07-08	2026-01-08	ACTIVE	96	14
110	2026-07-08	2026-02-08	ACTIVE	99	6
111	2026-05-08	2025-10-08	ACTIVE	99	8
57	2026-07-08	2025-12-08	PROSPECT	64	2
64	2026-02-08	2025-12-08	PROSPECT	68	10
71	2026-06-08	2026-01-08	PROSPECT	73	6
78	2026-05-08	2026-02-08	PROSPECT	78	2
85	2026-02-08	2025-11-08	PROSPECT	82	14
92	2026-05-08	2025-12-08	PROSPECT	87	11
99	2026-07-08	2025-10-08	PROSPECT	91	16
106	2026-05-08	2025-10-08	PROSPECT	96	6
113	2026-03-08	2025-11-08	PROSPECT	101	13
59	2026-06-08	2026-02-08	PROSPECT	65	2
75	2026-05-08	2026-01-08	PROSPECT	76	8
83	2026-02-08	2025-11-08	PROSPECT	81	16
91	2026-06-08	2025-12-08	PROSPECT	86	8
100	2026-06-08	2025-10-08	PROSPECT	92	13
108	2026-05-08	2025-09-08	PROSPECT	98	12
60	2026-06-08	2025-10-08	PROSPECT	66	7
69	2026-06-08	2025-12-08	PROSPECT	72	5
79	2026-07-08	2026-01-08	PROSPECT	79	15
88	2026-05-08	2025-11-08	PROSPECT	84	16
97	2026-05-08	2025-09-08	PROSPECT	90	1
107	2026-04-08	2025-09-08	PROSPECT	97	13
61	2026-05-08	2026-01-08	PROSPECT	66	11
72	2026-07-08	2026-01-08	PROSPECT	73	9
82	2026-06-08	2025-09-08	PROSPECT	80	6
94	2026-05-08	2026-01-08	PROSPECT	88	12
104	2026-03-08	2025-09-08	PROSPECT	95	9
62	2026-07-08	2025-10-08	PROSPECT	67	8
74	2026-03-08	2025-10-08	PROSPECT	75	10
87	2026-06-08	2026-02-08	PROSPECT	83	1
101	2026-05-08	2026-01-08	PROSPECT	93	2
63	2026-04-08	2025-11-08	PROSPECT	68	3
77	2026-07-08	2026-02-08	PROSPECT	77	3
93	2026-03-08	2025-09-08	PROSPECT	88	7
109	2026-04-08	2025-10-08	PROSPECT	98	2
65	2026-03-08	2026-01-08	PROSPECT	69	12
81	2026-07-08	2026-01-08	PROSPECT	80	5
98	2026-02-08	2025-09-08	PROSPECT	91	8
112	2026-02-08	2025-10-08	ACTIVE	100	4
115	2026-03-08	2025-10-08	ACTIVE	102	2
123	2026-05-08	2025-11-08	ACTIVE	108	9
124	2026-05-08	2026-01-08	ACTIVE	108	14
126	2026-05-08	2026-02-08	ACTIVE	110	6
129	2026-07-08	2026-02-08	ACTIVE	111	6
132	2026-03-08	2025-12-08	ACTIVE	113	10
136	2026-07-08	2025-11-08	ACTIVE	115	2
140	2026-03-08	2026-01-08	ACTIVE	118	15
142	2026-03-08	2026-02-08	ACTIVE	119	5
145	2026-03-08	2026-01-08	ACTIVE	121	5
147	2026-04-08	2025-12-08	ACTIVE	123	16
151	2026-04-08	2026-01-08	ACTIVE	127	2
152	2026-03-08	2025-11-08	ACTIVE	128	15
158	2026-03-08	2026-02-08	ACTIVE	131	6
159	2026-07-08	2026-02-08	ACTIVE	132	4
161	2026-07-08	2025-09-08	ACTIVE	133	7
166	2026-07-08	2025-11-08	ACTIVE	136	12
167	2026-05-08	2025-12-08	ACTIVE	137	12
120	2026-03-08	2025-11-08	PROSPECT	105	8
127	2026-05-08	2025-11-08	PROSPECT	110	9
134	2026-06-08	2025-12-08	PROSPECT	114	6
141	2026-07-08	2025-10-08	PROSPECT	118	6
148	2026-02-08	2025-11-08	PROSPECT	124	2
155	2026-04-08	2025-11-08	PROSPECT	129	5
162	2026-03-08	2025-12-08	PROSPECT	133	16
114	2026-02-08	2025-10-08	PROSPECT	101	6
122	2026-07-08	2025-12-08	PROSPECT	107	10
130	2026-02-08	2026-02-08	PROSPECT	112	2
138	2026-04-08	2025-10-08	PROSPECT	116	7
146	2026-04-08	2025-11-08	PROSPECT	122	3
154	2026-07-08	2025-10-08	PROSPECT	129	3
163	2026-06-08	2025-12-08	PROSPECT	134	6
116	2026-04-08	2026-02-08	PROSPECT	103	6
125	2026-04-08	2025-11-08	PROSPECT	109	9
135	2026-02-08	2026-01-08	PROSPECT	114	16
144	2026-05-08	2025-10-08	PROSPECT	121	11
153	2026-06-08	2026-01-08	PROSPECT	128	7
164	2026-03-08	2026-01-08	PROSPECT	134	11
117	2026-06-08	2025-12-08	PROSPECT	104	11
128	2026-06-08	2026-02-08	PROSPECT	111	1
139	2026-06-08	2025-12-08	PROSPECT	117	14
150	2026-03-08	2025-10-08	PROSPECT	126	7
160	2026-06-08	2025-12-08	PROSPECT	132	5
118	2026-05-08	2025-12-08	PROSPECT	104	15
131	2026-06-08	2026-02-08	PROSPECT	112	3
143	2026-07-08	2026-02-08	PROSPECT	120	12
157	2026-05-08	2025-11-08	PROSPECT	130	11
119	2026-06-08	2025-10-08	PROSPECT	105	16
133	2026-03-08	2025-10-08	PROSPECT	113	14
149	2026-07-08	2026-02-08	PROSPECT	125	1
165	2026-04-08	2025-10-08	PROSPECT	135	12
121	2026-03-08	2025-11-08	PROSPECT	106	2
137	2026-05-08	2025-11-08	PROSPECT	116	10
156	2026-05-08	2025-10-08	PROSPECT	130	12
168	2026-02-08	2026-01-08	ACTIVE	137	7
170	2026-04-08	2025-11-08	ACTIVE	138	9
172	2026-07-08	2026-02-08	ACTIVE	139	1
180	2026-06-08	2026-02-08	ACTIVE	144	12
181	2026-03-08	2026-02-08	ACTIVE	145	11
184	2026-05-08	2025-11-08	ACTIVE	147	13
186	2026-07-08	2026-01-08	ACTIVE	148	10
189	2026-07-08	2025-10-08	ACTIVE	151	14
193	2026-04-08	2026-01-08	ACTIVE	154	16
198	2026-03-08	2026-02-08	ACTIVE	157	11
199	2026-06-08	2025-12-08	ACTIVE	158	7
202	2026-06-08	2025-11-08	ACTIVE	161	15
205	2026-05-08	2026-02-08	ACTIVE	162	4
208	2026-06-08	2026-01-08	ACTIVE	164	6
209	2026-02-08	2026-02-08	ACTIVE	165	15
215	2026-02-08	2025-12-08	ACTIVE	169	2
216	2026-06-08	2025-11-08	ACTIVE	169	1
219	2026-02-08	2026-01-08	ACTIVE	171	10
176	2026-04-08	2025-12-08	PROSPECT	141	4
183	2026-07-08	2026-02-08	PROSPECT	146	9
190	2026-02-08	2025-11-08	PROSPECT	152	5
197	2026-04-08	2026-02-08	PROSPECT	156	7
204	2026-05-08	2026-01-08	PROSPECT	162	10
211	2026-04-08	2025-10-08	PROSPECT	166	16
218	2026-04-08	2025-12-08	PROSPECT	171	2
171	2026-07-08	2025-12-08	PROSPECT	139	3
179	2026-03-08	2026-01-08	PROSPECT	144	1
187	2026-07-08	2025-11-08	PROSPECT	149	1
195	2026-02-08	2026-02-08	PROSPECT	155	7
203	2026-02-08	2025-11-08	PROSPECT	161	9
212	2026-05-08	2025-11-08	PROSPECT	166	15
220	2026-07-08	2026-01-08	PROSPECT	172	6
173	2026-06-08	2025-11-08	PROSPECT	140	16
182	2026-06-08	2026-02-08	PROSPECT	145	4
192	2026-02-08	2026-02-08	PROSPECT	153	6
201	2026-05-08	2026-02-08	PROSPECT	160	8
210	2026-07-08	2025-12-08	PROSPECT	165	1
221	2026-05-08	2026-01-08	PROSPECT	173	2
174	2026-06-08	2025-09-08	PROSPECT	140	1
185	2026-04-08	2025-12-08	PROSPECT	148	8
196	2026-06-08	2025-09-08	PROSPECT	156	8
207	2026-03-08	2025-10-08	PROSPECT	164	11
217	2026-05-08	2026-01-08	PROSPECT	170	14
175	2026-02-08	2025-10-08	PROSPECT	141	13
188	2026-06-08	2026-01-08	PROSPECT	150	12
200	2026-02-08	2025-11-08	PROSPECT	159	12
214	2026-07-08	2025-11-08	PROSPECT	168	9
177	2026-05-08	2025-12-08	PROSPECT	142	1
191	2026-07-08	2025-12-08	PROSPECT	152	6
206	2026-03-08	2025-09-08	PROSPECT	163	7
222	2026-04-08	2026-01-08	PROSPECT	173	15
178	2026-05-08	2025-11-08	PROSPECT	143	2
194	2026-02-08	2025-12-08	PROSPECT	154	11
213	2026-06-08	2025-10-08	PROSPECT	167	7
223	2026-03-08	2025-11-08	ACTIVE	174	5
224	2026-07-08	2026-01-08	ACTIVE	174	6
226	2026-06-08	2026-01-08	ACTIVE	176	1
227	2026-07-08	2026-02-08	ACTIVE	177	15
229	2026-07-08	2026-01-08	ACTIVE	178	8
237	2026-02-08	2025-10-08	ACTIVE	183	16
238	2026-04-08	2026-02-08	ACTIVE	184	12
241	2026-05-08	2025-12-08	ACTIVE	186	14
243	2026-04-08	2026-01-08	ACTIVE	188	4
247	2026-04-08	2025-09-08	ACTIVE	191	1
250	2026-06-08	2025-12-08	ACTIVE	192	8
255	2026-07-08	2026-01-08	ACTIVE	197	4
256	2026-07-08	2025-09-08	ACTIVE	198	16
259	2026-05-08	2025-09-08	ACTIVE	200	8
262	2026-02-08	2026-01-08	ACTIVE	202	3
265	2026-04-08	2025-11-08	ACTIVE	204	3
266	2026-07-08	2026-02-08	ACTIVE	205	5
272	2026-05-08	2025-11-08	ACTIVE	209	7
273	2026-03-08	2025-11-08	ACTIVE	209	4
276	2026-06-08	2025-10-08	ACTIVE	211	16
1	2026-03-08	2025-11-08	PROSPECT	22	10
232	2026-06-08	2025-09-08	PROSPECT	180	15
239	2026-04-08	2025-09-08	PROSPECT	185	6
246	2026-02-08	2025-09-08	PROSPECT	190	11
253	2026-02-08	2026-02-08	PROSPECT	195	13
260	2026-05-08	2026-02-08	PROSPECT	201	2
267	2026-06-08	2025-11-08	PROSPECT	206	12
274	2026-07-08	2025-10-08	PROSPECT	210	15
228	2026-06-08	2025-09-08	PROSPECT	178	3
236	2026-04-08	2026-02-08	PROSPECT	182	14
244	2026-03-08	2025-11-08	PROSPECT	189	7
252	2026-02-08	2025-12-08	PROSPECT	194	4
261	2026-06-08	2026-01-08	PROSPECT	201	12
269	2026-06-08	2025-10-08	PROSPECT	207	16
230	2026-07-08	2025-11-08	PROSPECT	179	4
240	2026-02-08	2025-12-08	PROSPECT	186	7
249	2026-02-08	2025-10-08	PROSPECT	192	5
258	2026-06-08	2025-11-08	PROSPECT	199	8
268	2026-06-08	2025-09-08	PROSPECT	206	15
231	2026-07-08	2026-02-08	PROSPECT	180	12
242	2026-04-08	2025-11-08	PROSPECT	187	1
254	2026-07-08	2025-11-08	PROSPECT	196	8
264	2026-07-08	2026-01-08	PROSPECT	204	16
275	2026-02-08	2025-11-08	PROSPECT	211	11
233	2026-02-08	2025-12-08	PROSPECT	181	14
245	2026-03-08	2025-12-08	PROSPECT	189	16
257	2026-07-08	2026-01-08	PROSPECT	199	11
271	2026-06-08	2025-11-08	PROSPECT	208	11
234	2026-06-08	2026-02-08	PROSPECT	181	8
248	2026-03-08	2026-01-08	PROSPECT	191	15
263	2026-06-08	2025-12-08	PROSPECT	203	6
235	2026-06-08	2025-09-08	PROSPECT	182	6
251	2026-04-08	2025-10-08	PROSPECT	193	7
270	2026-07-08	2025-12-08	PROSPECT	208	8
169	2026-02-08	2026-01-08	PROSPECT	138	5
225	2026-02-08	2025-10-08	PROSPECT	175	2
67	2026-05-08	2025-09-08	PROSPECT	71	8
277	2026-12-30	2029-06-08	PROSPECT	106	15
\.


--
-- Data for Name: employees; Type: TABLE DATA; Schema: public; Owner: atlas
--

COPY public.employees (id, cost_center, email, future_manager, gender, grade, hire_date, hiring_type, is_active, job_level, legal_entity, location, name, nationality, oracle_id, parent_tower, primary_skill, reason_of_leave, resignation_date, secondary_skill, status, title, tower, manager_id) FROM stdin;
12	\N	mahmoud.ibrahim410@company.com	\N	Male	N3	2026-02-05	On-Payroll	t	Department Manager	GS	Egypt	Mahmoud Ibrahim	Egyptian	1011	OT	Machine Learning	\N	\N	Machine Learning	ACTIVE	Automation Manager	Automation and Control	5
13	\N	layla.hassan897@company.com	\N	Female	N3	2023-02-10	On-Payroll	t	Department Manager	GS	KSA	Layla Hassan	Egyptian	1012	OT	DevOps	\N	\N	Cloud Services	ACTIVE	Control Systems Manager	Automation and Control	5
14	\N	nour.mostafa794@company.com	\N	Female	N4	2022-01-01	On-Payroll	t	Team Lead	GS	Egypt	Nour Mostafa	Egyptian	1013	EPIS	Data Engineering	\N	\N	Agile	ACTIVE	Cloud Team Lead	Cloud & Core Infrastructure Services	6
15	\N	fatma.ahmed390@company.com	\N	Female	N4	2021-10-19	On-Payroll	t	Team Lead	GS	KSA	Fatma Ahmed	Egyptian	1014	EPIS	Python	\N	\N	Agile	ACTIVE	Security Team Lead	Cloud & Core Infrastructure Services	7
16	\N	hana.mohamed733@company.com	\N	Female	N4	2025-11-07	On-Payroll	t	Team Lead	GS	UAE	Hana Mohamed	Egyptian	1015	Application	Testing	\N	\N	Java	ACTIVE	Frontend Team Lead	Testing	8
17	\N	aisha.ali200@company.com	\N	Female	N4	2021-02-27	On-Payroll	t	Team Lead	GS	UAE	Aisha Ali	Egyptian	1016	Application	Cybersecurity	\N	\N	Business Analysis	ACTIVE	Testing Team Lead	Testing	9
18	\N	mona.hassan593@company.com	\N	Female	N4	2024-10-17	On-Payroll	t	Team Lead	GS	UAE	Mona Hassan	Egyptian	1017	Data&Agility	Network Engineering	\N	\N	Cloud Services	ACTIVE	Scrum Master	Agility	10
19	\N	ahmed.abdel747@company.com	\N	Male	N4	2024-01-11	On-Payroll	t	Team Lead	GS	Egypt	Ahmed Abdel	Egyptian	1018	Data&Agility	Testing	\N	\N	React	ACTIVE	Data Engineering Lead	Agility	11
20	\N	mohamed.kamal823@company.com	\N	Male	N4	2022-06-21	On-Payroll	t	Team Lead	GS	UAE	Mohamed Kamal	Egyptian	1019	OT	React	\N	\N	Cybersecurity	ACTIVE	PLC Team Lead	Automation and Control	12
21	\N	omar.salem870@company.com	\N	Male	N4	2023-06-01	On-Payroll	t	Team Lead	GS	KSA	Omar Salem	Egyptian	1020	OT	Data Engineering	\N	\N	Cybersecurity	ACTIVE	SCADA Team Lead	Automation and Control	13
23	\N	ali.nasser606@company.com	\N	Male	7	2023-06-26	On-Payroll	t	Specialist	GS	Egypt	Ali Nasser	Egyptian	1022	EPIS	Network Engineering	\N	\N	Data Engineering	ACTIVE	DevOps Engineer	Cloud & Core Infrastructure Services	7
25	\N	hassan.nasser525@company.com	\N	Male	3	2022-05-07	On-Payroll	t	Specialist	GS	KSA	Hassan Nasser	Egyptian	1024	Application	Project Management	\N	\N	Business Analysis	ACTIVE	Cloud Services Engineer	Testing	9
27	\N	hassan.salem481@company.com	\N	Male	C	2025-03-03	On-Payroll	t	Specialist	GS	UAE	Hassan Salem	Egyptian	1026	Data&Agility	Machine Learning	\N	\N	Cloud Services	ACTIVE	React Engineer	Agility	11
30	\N	aisha.salem589@company.com	\N	Female	7	2021-03-21	On-Payroll	t	Specialist	GS	Egypt	Aisha Salem	Egyptian	1029	EPIS	Java	\N	\N	Agile	ACTIVE	Cloud Services Engineer	Cloud & Core Infrastructure Services	14
35	\N	khaled.el-sayed416@company.com	\N	Male	6	2024-12-30	On-Payroll	t	Specialist	GS	KSA	Khaled El-Sayed	Egyptian	1034	Data&Agility	Machine Learning	\N	\N	Testing	ACTIVE	Python Engineer	Agility	19
31	\N	ahmed.hassan398@company.com	\N	Male	3	2021-04-09	On-Payroll	t	Specialist	GS	KSA	Ahmed Hassan	Egyptian	1030	EPIS	React	\N	\N	Testing	ACTIVE	DevOps Engineer	Cloud & Core Infrastructure Services	15
29	\N	fatma.mohamed741@company.com	\N	Female	5	2025-07-11	On-Payroll	t	Specialist	GS	Egypt	Fatma Mohamed	Egyptian	1028	OT	Cybersecurity	\N	\N	Cybersecurity	ACTIVE	Cybersecurity Engineer	Automation and Control	13
4	\N	khaled.nasser32@company.com	\N	Male	N2	2025-07-14	On-Payroll	t	Head	GS	KSA	Khaled Nasser	Egyptian	1003	Data&Agility	Java	\N	\N	Testing	ACTIVE	VP of Data & Agility	Agility	1
9	\N	dina.salem379@company.com	\N	Female	N3	2020-09-12	On-Payroll	t	Department Manager	GS	KSA	Dina Salem	Egyptian	1008	Application	DevOps	\N	\N	Machine Learning	LONG_LEAVE	QA Manager	Testing	3
5	\N	hassan.farouk400@company.com	\N	Male	N2	2022-09-14	On-Payroll	t	Head	GS	Egypt	Hassan Farouk	Egyptian	1004	OT	Python	\N	\N	Cybersecurity	ACTIVE	VP of Operations Technology	Automation and Control	1
2	\N	mohamed.hassan525@company.com	\N	Male	N2	2025-11-07	On-Payroll	t	Head	GS	KSA	Mohamed Hassan	Egyptian	1001	EPIS	Python	\N	\N	Project Management	ACTIVE	VP of Infrastructure	Cloud & Core Infrastructure Services	1
22	\N	layla.kamal522@company.com	\N	Female	5	2024-11-14	On-Payroll	t	Specialist	GS	Egypt	Layla Kamal	Egyptian	1021	EPIS	Cloud Services	\N	\N	Java	ACTIVE	DevOps Engineer	Cloud & Core Infrastructure Services	6
7	\N	ali.mahmoud730@company.com	\N	Male	N3	2025-07-06	On-Payroll	t	Department Manager	GS	UAE	Ali Mahmoud	Egyptian	1006	EPIS	Data Engineering	\N	\N	Angular	ACTIVE	Infrastructure Manager	Cloud & Core Infrastructure Services	2
28	\N	ali.mostafa610@company.com	\N	Male	3	2021-12-02	On-Payroll	t	Specialist	GS	UAE	Ali Mostafa	Egyptian	1027	OT	Python	\N	\N	Testing	ACTIVE	Cloud Services Engineer	Automation and Control	12
10	\N	youssef.kamal534@company.com	\N	Male	N3	2024-11-22	On-Payroll	t	Department Manager	GS	KSA	Youssef Kamal	Egyptian	1009	Data&Agility	Business Analysis	\N	\N	Network Engineering	ACTIVE	Agility Manager	Agility	4
33	\N	fatma.kamal654@company.com	\N	Female	3	2024-10-08	On-Payroll	t	Specialist	GS	KSA	Fatma Kamal	Egyptian	1032	Application	Data Engineering	\N	\N	Project Management	ACTIVE	React Engineer	Testing	17
3	\N	sara.ibrahim182@company.com	\N	Female	N2	2024-10-20	On-Payroll	t	Head	GS	Egypt	Sara Ibrahim	Egyptian	1002	Application	Cloud Services	\N	\N	Java	ACTIVE	VP of Applications	Testing	1
8	\N	marwa.ali727@company.com	\N	Female	N3	2022-12-02	On-Payroll	t	Department Manager	GS	KSA	Marwa Ali	Egyptian	1007	Application	Testing	\N	\N	DevOps	ACTIVE	Development Manager	Testing	3
26	\N	mahmoud.farouk803@company.com	\N	Male	C	2021-05-04	On-Payroll	t	Specialist	GS	KSA	Mahmoud Farouk	Egyptian	1025	Data&Agility	Java	\N	\N	Machine Learning	RESIGNED	Project Management Engineer	Agility	10
11	\N	reem.rashad193@company.com	\N	Female	N3	2022-12-06	On-Payroll	t	Department Manager	GS	KSA	Reem Rashad	Egyptian	1010	Data&Agility	Java	\N	\N	Angular	MATERNITY	Scrum Manager	Agility	4
24	\N	layla.ahmed923@company.com	\N	Female	3	2025-08-20	On-Payroll	t	Specialist	GS	KSA	Layla Ahmed	Egyptian	1023	Application	React	\N	\N	Data Engineering	ACTIVE	Java Engineer	Testing	8
6	\N	omar.mohamed241@company.com	\N	Male	N3	2025-06-07	On-Payroll	t	Department Manager	GS	KSA	Omar Mohamed	Egyptian	1005	EPIS	Java	\N	\N	Project Management	ACTIVE	Cloud Services Manager	Cloud & Core Infrastructure Services	2
32	\N	fatma.mahmoud374@company.com	\N	Female	C	2022-11-08	On-Payroll	t	Specialist	GS	Egypt	Fatma Mahmoud	Egyptian	1031	Application	Python	\N	\N	Java	ACTIVE	React Engineer	Testing	16
36	\N	layla.nasser659@company.com	\N	Female	6	2022-07-29	On-Payroll	t	Specialist	GS	UAE	Layla Nasser	Egyptian	1035	OT	Business Analysis	\N	\N	Cybersecurity	ACTIVE	Data Engineering Engineer	Automation and Control	20
37	\N	youssef.ibrahim88@company.com	\N	Male	C	2021-05-15	On-Payroll	t	Specialist	GS	UAE	Youssef Ibrahim	Egyptian	1036	OT	Machine Learning	\N	\N	Network Engineering	ACTIVE	Project Management Engineer	Automation and Control	21
38	\N	layla.kamal425@company.com	\N	Female	6	2024-11-06	On-Payroll	t	Specialist	GS	UAE	Layla Kamal	Egyptian	1037	EPIS	Machine Learning	\N	\N	Agile	ACTIVE	Python Engineer	Cloud & Core Infrastructure Services	6
39	\N	dina.mahmoud760@company.com	\N	Female	5	2023-03-11	On-Payroll	t	Specialist	GS	KSA	Dina Mahmoud	Egyptian	1038	EPIS	Testing	\N	\N	Project Management	ACTIVE	Network Engineering Engineer	Cloud & Core Infrastructure Services	7
40	\N	fatma.salem340@company.com	\N	Female	3	2025-09-02	On-Payroll	t	Specialist	GS	UAE	Fatma Salem	Egyptian	1039	Application	Cloud Services	\N	\N	Agile	ACTIVE	Testing Engineer	Testing	8
42	\N	youssef.salem590@company.com	\N	Male	5	2020-12-14	On-Payroll	t	Specialist	GS	KSA	Youssef Salem	Egyptian	1041	Data&Agility	Java	\N	\N	Cloud Services	ACTIVE	React Engineer	Agility	10
44	\N	aisha.ibrahim633@company.com	\N	Female	6	2020-10-19	On-Payroll	t	Specialist	GS	UAE	Aisha Ibrahim	Egyptian	1043	OT	Project Management	\N	\N	Cybersecurity	ACTIVE	Python Engineer	Automation and Control	12
45	\N	hassan.abdel955@company.com	\N	Male	C	2022-04-23	On-Payroll	t	Specialist	GS	UAE	Hassan Abdel	Egyptian	1044	OT	Network Engineering	\N	\N	DevOps	ACTIVE	Cybersecurity Engineer	Automation and Control	13
46	\N	layla.abdel518@company.com	\N	Female	4	2025-04-12	On-Payroll	t	Specialist	GS	Egypt	Layla Abdel	Egyptian	1045	EPIS	React	\N	\N	Agile	ACTIVE	Cybersecurity Engineer	Cloud & Core Infrastructure Services	14
47	\N	hana.el-sayed36@company.com	\N	Female	C	2023-12-30	On-Payroll	t	Specialist	GS	KSA	Hana El-Sayed	Egyptian	1046	EPIS	Java	\N	\N	React	ACTIVE	Business Analysis Engineer	Cloud & Core Infrastructure Services	15
48	\N	omar.el-sayed56@company.com	\N	Male	3	2023-11-18	On-Payroll	t	Specialist	GS	KSA	Omar El-Sayed	Egyptian	1047	Application	Agile	\N	\N	Java	ACTIVE	React Engineer	Testing	16
49	\N	ali.ali241@company.com	\N	Male	3	2024-03-04	On-Payroll	t	Specialist	GS	KSA	Ali Ali	Egyptian	1048	Application	Cloud Services	\N	\N	Data Engineering	ACTIVE	Agile Engineer	Testing	17
50	\N	omar.hassan577@company.com	\N	Male	7	2022-12-02	On-Payroll	t	Specialist	GS	KSA	Omar Hassan	Egyptian	1049	Data&Agility	Angular	\N	\N	Testing	ACTIVE	Business Analysis Engineer	Agility	18
52	\N	hassan.mohamed588@company.com	\N	Male	5	2020-10-12	On-Payroll	t	Specialist	GS	Egypt	Hassan Mohamed	Egyptian	1051	OT	Cybersecurity	\N	\N	Java	ACTIVE	Agile Engineer	Automation and Control	20
53	\N	aisha.salem515@company.com	\N	Female	5	2023-10-14	On-Payroll	t	Specialist	GS	KSA	Aisha Salem	Egyptian	1052	OT	Network Engineering	\N	\N	Data Engineering	ACTIVE	Network Engineering Engineer	Automation and Control	21
55	\N	mohamed.farouk968@company.com	\N	Male	7	2024-02-19	On-Payroll	t	Specialist	GS	KSA	Mohamed Farouk	Egyptian	1054	EPIS	DevOps	\N	\N	Testing	ACTIVE	Business Analysis Engineer	Cloud & Core Infrastructure Services	7
56	\N	dina.mahmoud94@company.com	\N	Female	3	2021-01-15	On-Payroll	t	Specialist	GS	KSA	Dina Mahmoud	Egyptian	1055	Application	Project Management	\N	\N	React	ACTIVE	Cloud Services Engineer	Testing	8
68	\N	layla.mostafa51@company.com	\N	Female	7	2023-08-25	On-Payroll	t	Specialist	GS	UAE	Layla Mostafa	Egyptian	1067	OT	DevOps	\N	\N	Data Engineering	ACTIVE	DevOps Engineer	Automation and Control	20
104	\N	mahmoud.salem739@company.com	\N	Male	7	2025-09-01	On-Payroll	t	Specialist	GS	UAE	Mahmoud Salem	Egyptian	1103	Application	Cybersecurity	\N	\N	Testing	ACTIVE	Testing Engineer	Testing	8
69	\N	aisha.nasser182@company.com	\N	Female	3	2024-06-28	On-Payroll	t	Specialist	GS	UAE	Aisha Nasser	Egyptian	1068	OT	Testing	\N	\N	Data Engineering	ACTIVE	Java Engineer	Automation and Control	21
58	\N	marwa.ahmed408@company.com	\N	Female	4	2021-11-20	On-Payroll	t	Specialist	GS	UAE	Marwa Ahmed	Egyptian	1057	Data&Agility	Angular	\N	\N	Network Engineering	ACTIVE	Python Engineer	Agility	10
59	\N	sara.ibrahim391@company.com	\N	Female	3	2024-04-27	On-Payroll	t	Specialist	GS	Egypt	Sara Ibrahim	Egyptian	1058	Data&Agility	DevOps	\N	\N	Angular	ACTIVE	Project Management Engineer	Agility	11
60	\N	mohamed.hassan678@company.com	\N	Male	3	2025-10-24	On-Payroll	t	Specialist	GS	UAE	Mohamed Hassan	Egyptian	1059	OT	Cybersecurity	\N	\N	Angular	ACTIVE	Data Engineering Engineer	Automation and Control	12
62	\N	dina.mahmoud746@company.com	\N	Female	5	2021-09-06	On-Payroll	t	Specialist	GS	KSA	Dina Mahmoud	Egyptian	1061	EPIS	Java	\N	\N	DevOps	ACTIVE	DevOps Engineer	Cloud & Core Infrastructure Services	14
63	\N	omar.nasser425@company.com	\N	Male	3	2024-10-31	On-Payroll	t	Specialist	GS	UAE	Omar Nasser	Egyptian	1062	EPIS	Data Engineering	\N	\N	Business Analysis	ACTIVE	Agile Engineer	Cloud & Core Infrastructure Services	15
64	\N	sara.ali254@company.com	\N	Female	6	2023-03-29	On-Payroll	t	Specialist	GS	Egypt	Sara Ali	Egyptian	1063	Application	Cloud Services	\N	\N	Business Analysis	ACTIVE	Testing Engineer	Testing	16
66	\N	nour.el-sayed970@company.com	\N	Female	C	2021-07-10	On-Payroll	t	Specialist	GS	UAE	Nour El-Sayed	Egyptian	1065	Data&Agility	Testing	\N	\N	Network Engineering	ACTIVE	Cloud Services Engineer	Agility	18
57	\N	layla.nasser974@company.com	\N	Female	C	2023-01-01	On-Payroll	t	Specialist	GS	Egypt	Layla Nasser	Egyptian	1056	Application	Network Engineering	\N	\N	Business Analysis	ACTIVE	Agile Engineer	Testing	9
51	\N	mona.rashad895@company.com	\N	Female	5	2023-10-10	On-Payroll	t	Specialist	GS	Egypt	Mona Rashad	Egyptian	1050	Data&Agility	Agile	\N	\N	Data Engineering	ACTIVE	Cybersecurity Engineer	Agility	19
54	\N	mahmoud.mohamed546@company.com	\N	Male	5	2025-11-23	On-Payroll	t	Specialist	GS	KSA	Mahmoud Mohamed	Egyptian	1053	EPIS	Cybersecurity	\N	\N	Data Engineering	ACTIVE	React Engineer	Cloud & Core Infrastructure Services	6
43	\N	fatma.farouk241@company.com	\N	Female	6	2023-01-01	On-Payroll	t	Specialist	GS	KSA	Fatma Farouk	Egyptian	1042	Data&Agility	Python	\N	\N	Agile	ACTIVE	DevOps Engineer	Agility	11
67	\N	fatma.el-sayed721@company.com	\N	Female	7	2023-02-16	On-Payroll	t	Specialist	GS	KSA	Fatma El-Sayed	Egyptian	1066	Data&Agility	React	\N	\N	Agile	LONG_LEAVE	Cybersecurity Engineer	Agility	19
65	\N	nour.ali74@company.com	\N	Female	4	2024-04-06	On-Payroll	t	Specialist	GS	Egypt	Nour Ali	Egyptian	1064	Application	Cloud Services	\N	\N	Cloud Services	MATERNITY	React Engineer	Testing	17
70	\N	marwa.farouk1@company.com	\N	Female	6	2022-04-24	On-Payroll	t	Specialist	GS	UAE	Marwa Farouk	Egyptian	1069	EPIS	Machine Learning	\N	\N	Angular	ACTIVE	Project Management Engineer	Cloud & Core Infrastructure Services	6
71	\N	youssef.salem848@company.com	\N	Male	5	2024-09-08	On-Payroll	t	Specialist	GS	KSA	Youssef Salem	Egyptian	1070	EPIS	Cybersecurity	\N	\N	Network Engineering	ACTIVE	Network Engineering Engineer	Cloud & Core Infrastructure Services	7
72	\N	omar.ali967@company.com	\N	Male	4	2024-08-09	On-Payroll	t	Specialist	GS	KSA	Omar Ali	Egyptian	1071	Application	Cybersecurity	\N	\N	DevOps	ACTIVE	Machine Learning Engineer	Testing	8
73	\N	mohamed.kamal657@company.com	\N	Male	3	2026-01-24	On-Payroll	t	Specialist	GS	Egypt	Mohamed Kamal	Egyptian	1072	Application	Data Engineering	\N	\N	Java	ACTIVE	Business Analysis Engineer	Testing	9
74	\N	mahmoud.abdel282@company.com	\N	Male	4	2025-08-22	On-Payroll	t	Specialist	GS	Egypt	Mahmoud Abdel	Egyptian	1073	Data&Agility	DevOps	\N	\N	Project Management	ACTIVE	Cybersecurity Engineer	Agility	10
76	\N	layla.mohamed424@company.com	\N	Female	C	2023-02-15	On-Payroll	t	Specialist	GS	UAE	Layla Mohamed	Egyptian	1075	OT	Angular	\N	\N	Agile	ACTIVE	Cloud Services Engineer	Automation and Control	12
77	\N	reem.el-sayed805@company.com	\N	Female	6	2023-12-03	On-Payroll	t	Specialist	GS	Egypt	Reem El-Sayed	Egyptian	1076	OT	Business Analysis	\N	\N	Machine Learning	ACTIVE	Machine Learning Engineer	Automation and Control	13
78	\N	youssef.ali629@company.com	\N	Male	C	2022-08-20	On-Payroll	t	Specialist	GS	Egypt	Youssef Ali	Egyptian	1077	EPIS	Network Engineering	\N	\N	Network Engineering	ACTIVE	Machine Learning Engineer	Cloud & Core Infrastructure Services	14
80	\N	youssef.mahmoud838@company.com	\N	Male	C	2020-10-14	On-Payroll	t	Specialist	GS	Egypt	Youssef Mahmoud	Egyptian	1079	Application	Python	\N	\N	Data Engineering	ACTIVE	Machine Learning Engineer	Testing	16
97	\N	ali.ali210@company.com	\N	Male	6	2024-07-08	On-Payroll	t	Specialist	GS	Egypt	Ali Ali	Egyptian	1096	Application	Python	\N	\N	Machine Learning	ACTIVE	Cybersecurity Engineer	Testing	17
101	\N	nour.hassan697@company.com	\N	Female	7	2024-08-02	On-Payroll	t	Specialist	GS	UAE	Nour Hassan	Egyptian	1100	OT	Network Engineering	\N	\N	React	ACTIVE	Cybersecurity Engineer	Automation and Control	21
102	\N	mohamed.el-sayed871@company.com	\N	Male	C	2025-07-14	On-Payroll	t	Specialist	GS	KSA	Mohamed El-Sayed	Egyptian	1101	EPIS	Python	\N	\N	Cloud Services	ACTIVE	Project Management Engineer	Cloud & Core Infrastructure Services	6
82	\N	layla.farouk578@company.com	\N	Female	6	2022-08-12	On-Payroll	t	Specialist	GS	UAE	Layla Farouk	Egyptian	1081	Data&Agility	Angular	\N	\N	Project Management	MATERNITY	Cloud Services Engineer	Agility	18
87	\N	aisha.salem24@company.com	\N	Female	4	2023-01-22	On-Payroll	t	Specialist	GS	UAE	Aisha Salem	Egyptian	1086	EPIS	Data Engineering	\N	\N	Angular	RESIGNED	Data Engineering Engineer	Cloud & Core Infrastructure Services	7
84	\N	mona.rashad109@company.com	\N	Female	C	2022-02-08	On-Payroll	t	Specialist	GS	KSA	Mona Rashad	Egyptian	1083	OT	Machine Learning	\N	\N	Testing	ACTIVE	Cloud Services Engineer	Automation and Control	20
83	\N	hana.salem221@company.com	\N	Female	3	2024-07-08	On-Payroll	t	Specialist	GS	KSA	Hana Salem	Egyptian	1082	Data&Agility	Python	\N	\N	Agile	ACTIVE	Python Engineer	Agility	19
103	\N	layla.ibrahim601@company.com	\N	Female	4	2025-11-09	On-Payroll	t	Specialist	GS	KSA	Layla Ibrahim	Egyptian	1102	EPIS	DevOps	\N	\N	Data Engineering	ACTIVE	Java Engineer	Cloud & Core Infrastructure Services	7
85	\N	hana.el-sayed428@company.com	\N	Female	4	2021-04-07	On-Payroll	t	Specialist	GS	KSA	Hana El-Sayed	Egyptian	1084	OT	Data Engineering	\N	\N	React	ACTIVE	Testing Engineer	Automation and Control	21
99	\N	ahmed.ibrahim29@company.com	\N	Male	6	2022-11-05	On-Payroll	t	Specialist	GS	KSA	Ahmed Ibrahim	Egyptian	1098	Data&Agility	Testing	\N	\N	Network Engineering	ACTIVE	Business Analysis Engineer	Agility	19
96	\N	layla.mostafa17@company.com	\N	Female	4	2020-11-17	On-Payroll	t	Specialist	GS	KSA	Layla Mostafa	Egyptian	1095	Application	Machine Learning	\N	\N	Testing	ACTIVE	Agile Engineer	Testing	16
79	\N	layla.kamal822@company.com	\N	Female	4	2021-11-08	On-Payroll	t	Specialist	GS	UAE	Layla Kamal	Egyptian	1078	EPIS	Angular	\N	\N	Machine Learning	ACTIVE	Cybersecurity Engineer	Cloud & Core Infrastructure Services	15
88	\N	aisha.el-sayed567@company.com	\N	Female	C	2026-01-19	On-Payroll	t	Specialist	GS	KSA	Aisha El-Sayed	Egyptian	1087	Application	DevOps	\N	\N	Data Engineering	ACTIVE	Java Engineer	Testing	8
89	\N	ahmed.abdel49@company.com	\N	Male	6	2022-01-29	On-Payroll	t	Specialist	GS	Egypt	Ahmed Abdel	Egyptian	1088	Application	Agile	\N	\N	Python	ACTIVE	Testing Engineer	Testing	9
92	\N	hassan.ahmed788@company.com	\N	Male	4	2023-06-04	On-Payroll	t	Specialist	GS	Egypt	Hassan Ahmed	Egyptian	1091	OT	Business Analysis	\N	\N	Project Management	ACTIVE	Testing Engineer	Automation and Control	12
90	\N	nour.mahmoud935@company.com	\N	Female	C	2021-09-04	On-Payroll	t	Specialist	GS	KSA	Nour Mahmoud	Egyptian	1089	Data&Agility	Project Management	\N	\N	Business Analysis	ACTIVE	Agile Engineer	Agility	10
95	\N	hassan.farouk364@company.com	\N	Male	6	2021-06-30	On-Payroll	t	Specialist	GS	UAE	Hassan Farouk	Egyptian	1094	EPIS	Testing	\N	\N	Python	ACTIVE	Data Engineering Engineer	Cloud & Core Infrastructure Services	15
98	\N	fatma.ali282@company.com	\N	Female	3	2022-01-18	On-Payroll	t	Specialist	GS	UAE	Fatma Ali	Egyptian	1097	Data&Agility	Machine Learning	\N	\N	Cybersecurity	ACTIVE	Data Engineering Engineer	Agility	18
93	\N	layla.nasser778@company.com	\N	Female	7	2022-12-10	On-Payroll	t	Specialist	GS	UAE	Layla Nasser	Egyptian	1092	OT	Python	\N	\N	Project Management	ACTIVE	Network Engineering Engineer	Automation and Control	13
94	\N	reem.mahmoud977@company.com	\N	Female	C	2023-02-05	On-Payroll	t	Specialist	GS	KSA	Reem Mahmoud	Egyptian	1093	EPIS	Python	\N	\N	React	LONG_LEAVE	React Engineer	Cloud & Core Infrastructure Services	14
91	\N	aisha.ibrahim304@company.com	\N	Female	5	2021-03-16	On-Payroll	t	Specialist	GS	Egypt	Aisha Ibrahim	Egyptian	1090	Data&Agility	React	\N	\N	Network Engineering	ACTIVE	Java Engineer	Agility	11
100	\N	hana.nasser441@company.com	\N	Female	C	2024-04-24	On-Payroll	t	Specialist	GS	Egypt	Hana Nasser	Egyptian	1099	OT	DevOps	\N	\N	Testing	ACTIVE	Project Management Engineer	Automation and Control	20
81	\N	nour.mohamed446@company.com	\N	Female	C	2023-08-16	On-Payroll	t	Specialist	GS	KSA	Nour Mohamed	Egyptian	1080	Application	Network Engineering	\N	\N	Cybersecurity	ACTIVE	Java Engineer	Testing	17
106	\N	layla.mostafa50@company.com	\N	Female	3	2021-02-17	On-Payroll	t	Specialist	GS	KSA	Layla Mostafa	Egyptian	1105	Data&Agility	Testing	\N	\N	Cloud Services	ACTIVE	Cloud Services Engineer	Agility	10
113	\N	reem.nasser18@company.com	\N	Female	4	2021-06-18	On-Payroll	t	Specialist	GS	KSA	Reem Nasser	Egyptian	1112	Application	Project Management	\N	\N	Testing	ACTIVE	Data Engineering Engineer	Testing	17
117	\N	mahmoud.ahmed794@company.com	\N	Male	5	2023-05-03	On-Payroll	t	Specialist	GS	UAE	Mahmoud Ahmed	Egyptian	1116	OT	Business Analysis	\N	\N	React	ACTIVE	Agile Engineer	Automation and Control	21
118	\N	youssef.mahmoud300@company.com	\N	Male	4	2025-05-28	On-Payroll	t	Specialist	GS	KSA	Youssef Mahmoud	Egyptian	1117	EPIS	Agile	\N	\N	DevOps	ACTIVE	Python Engineer	Cloud & Core Infrastructure Services	6
119	\N	mahmoud.mohamed162@company.com	\N	Male	5	2025-11-24	On-Payroll	t	Specialist	GS	Egypt	Mahmoud Mohamed	Egyptian	1118	EPIS	Project Management	\N	\N	Cybersecurity	ACTIVE	Cloud Services Engineer	Cloud & Core Infrastructure Services	7
120	\N	layla.ibrahim227@company.com	\N	Female	7	2024-09-15	On-Payroll	t	Specialist	GS	UAE	Layla Ibrahim	Egyptian	1119	Application	Testing	\N	\N	Angular	ACTIVE	Project Management Engineer	Testing	8
122	\N	sara.ibrahim574@company.com	\N	Female	4	2024-04-15	On-Payroll	t	Specialist	GS	KSA	Sara Ibrahim	Egyptian	1121	Data&Agility	Angular	\N	\N	Machine Learning	ACTIVE	Network Engineering Engineer	Agility	10
123	\N	ahmed.rashad856@company.com	\N	Male	3	2025-09-19	On-Payroll	t	Specialist	GS	Egypt	Ahmed Rashad	Egyptian	1122	Data&Agility	Cybersecurity	\N	\N	Java	ACTIVE	Cloud Services Engineer	Agility	11
124	\N	aisha.mostafa159@company.com	\N	Female	4	2023-09-20	On-Payroll	t	Specialist	GS	Egypt	Aisha Mostafa	Egyptian	1123	OT	Agile	\N	\N	Java	ACTIVE	Network Engineering Engineer	Automation and Control	12
125	\N	hassan.ali874@company.com	\N	Male	3	2022-08-20	On-Payroll	t	Specialist	GS	Egypt	Hassan Ali	Egyptian	1124	OT	Project Management	\N	\N	Cloud Services	ACTIVE	Project Management Engineer	Automation and Control	13
128	\N	mahmoud.abdel363@company.com	\N	Male	4	2021-07-04	On-Payroll	t	Specialist	GS	UAE	Mahmoud Abdel	Egyptian	1127	Application	Angular	\N	\N	Angular	ACTIVE	Angular Engineer	Testing	16
129	\N	fatma.el-sayed234@company.com	\N	Female	7	2024-09-11	On-Payroll	t	Specialist	GS	UAE	Fatma El-Sayed	Egyptian	1128	Application	Cybersecurity	\N	\N	Cloud Services	ACTIVE	Project Management Engineer	Testing	17
131	\N	ahmed.mohamed177@company.com	\N	Male	4	2021-01-27	On-Payroll	t	Specialist	GS	UAE	Ahmed Mohamed	Egyptian	1130	Data&Agility	Cloud Services	\N	\N	Angular	ACTIVE	Data Engineering Engineer	Agility	19
134	\N	mona.kamal480@company.com	\N	Female	3	2024-05-20	On-Payroll	t	Specialist	GS	UAE	Mona Kamal	Egyptian	1133	EPIS	Testing	\N	\N	Cloud Services	ACTIVE	Business Analysis Engineer	Cloud & Core Infrastructure Services	6
137	\N	nour.mostafa303@company.com	\N	Female	3	2022-05-03	On-Payroll	t	Specialist	GS	KSA	Nour Mostafa	Egyptian	1136	Application	Cloud Services	\N	\N	Machine Learning	ACTIVE	Project Management Engineer	Testing	9
135	\N	omar.rashad45@company.com	\N	Male	3	2023-05-26	On-Payroll	t	Specialist	GS	Egypt	Omar Rashad	Egyptian	1134	EPIS	Data Engineering	\N	\N	React	ACTIVE	Angular Engineer	Cloud & Core Infrastructure Services	7
105	\N	nour.mostafa291@company.com	\N	Female	4	2022-09-04	On-Payroll	t	Specialist	GS	Egypt	Nour Mostafa	Egyptian	1104	Application	Cybersecurity	\N	\N	Project Management	ACTIVE	Network Engineering Engineer	Testing	9
130	\N	layla.abdel69@company.com	\N	Female	C	2021-10-17	On-Payroll	t	Specialist	GS	KSA	Layla Abdel	Egyptian	1129	Data&Agility	Agile	\N	\N	DevOps	ACTIVE	Testing Engineer	Agility	18
133	\N	omar.ali29@company.com	\N	Male	7	2023-12-19	On-Payroll	t	Specialist	GS	KSA	Omar Ali	Egyptian	1132	OT	Machine Learning	\N	\N	Java	ACTIVE	Project Management Engineer	Automation and Control	21
114	\N	reem.farouk645@company.com	\N	Female	5	2024-04-07	On-Payroll	t	Specialist	GS	KSA	Reem Farouk	Egyptian	1113	Data&Agility	Cybersecurity	\N	\N	Agile	ACTIVE	Business Analysis Engineer	Agility	18
110	\N	hassan.ali104@company.com	\N	Male	6	2025-10-10	On-Payroll	t	Specialist	GS	Egypt	Hassan Ali	Egyptian	1109	EPIS	DevOps	\N	\N	Agile	RESIGNED	DevOps Engineer	Cloud & Core Infrastructure Services	14
111	\N	ali.kamal865@company.com	\N	Male	C	2024-01-08	On-Payroll	t	Specialist	GS	KSA	Ali Kamal	Egyptian	1110	EPIS	Testing	\N	\N	React	ACTIVE	DevOps Engineer	Cloud & Core Infrastructure Services	15
138	\N	reem.nasser643@company.com	\N	Female	4	2022-11-17	On-Payroll	t	Specialist	GS	KSA	Reem Nasser	Egyptian	1137	Data&Agility	Network Engineering	\N	\N	Agile	ACTIVE	Project Management Engineer	Agility	10
112	\N	hassan.rashad483@company.com	\N	Male	3	2025-06-08	On-Payroll	t	Specialist	GS	UAE	Hassan Rashad	Egyptian	1111	Application	Business Analysis	\N	\N	React	ACTIVE	Agile Engineer	Testing	16
116	\N	mahmoud.salem382@company.com	\N	Male	6	2024-10-01	On-Payroll	t	Specialist	GS	UAE	Mahmoud Salem	Egyptian	1115	OT	Cloud Services	\N	\N	Cybersecurity	LONG_LEAVE	Agile Engineer	Automation and Control	20
107	\N	ahmed.farouk284@company.com	\N	Male	6	2025-06-27	On-Payroll	t	Specialist	GS	UAE	Ahmed Farouk	Egyptian	1106	Data&Agility	Testing	\N	\N	Agile	ACTIVE	Angular Engineer	Agility	11
115	\N	omar.mostafa145@company.com	\N	Male	7	2024-06-08	On-Payroll	t	Specialist	GS	Egypt	Omar Mostafa	Egyptian	1114	Data&Agility	Cybersecurity	\N	\N	Project Management	LONG_LEAVE	Angular Engineer	Agility	19
132	\N	ali.ahmed737@company.com	\N	Male	5	2025-09-29	On-Payroll	t	Specialist	GS	Egypt	Ali Ahmed	Egyptian	1131	OT	Cybersecurity	\N	\N	Machine Learning	ACTIVE	Testing Engineer	Automation and Control	20
121	\N	ali.ahmed716@company.com	\N	Male	3	2024-04-19	On-Payroll	t	Specialist	GS	KSA	Ali Ahmed	Egyptian	1120	Application	Angular	\N	\N	Cloud Services	ACTIVE	Testing Engineer	Testing	9
127	\N	omar.mohamed105@company.com	\N	Male	4	2021-11-06	On-Payroll	t	Specialist	GS	KSA	Omar Mohamed	Egyptian	1126	EPIS	Angular	\N	\N	Angular	ACTIVE	Data Engineering Engineer	Cloud & Core Infrastructure Services	15
108	\N	hana.farouk882@company.com	\N	Female	6	2021-10-07	On-Payroll	t	Specialist	GS	KSA	Hana Farouk	Egyptian	1107	OT	Project Management	\N	\N	Business Analysis	ACTIVE	Network Engineering Engineer	Automation and Control	12
109	\N	sara.abdel949@company.com	\N	Female	3	2022-12-02	On-Payroll	t	Specialist	GS	KSA	Sara Abdel	Egyptian	1108	OT	Machine Learning	\N	\N	Cloud Services	ACTIVE	Testing Engineer	Automation and Control	13
136	\N	nour.ibrahim316@company.com	\N	Female	C	2024-10-25	On-Payroll	t	Specialist	GS	Egypt	Nour Ibrahim	Egyptian	1135	Application	Cloud Services	\N	\N	Project Management	ACTIVE	Python Engineer	Testing	8
139	\N	sara.mahmoud68@company.com	\N	Female	6	2024-10-21	On-Payroll	t	Specialist	GS	KSA	Sara Mahmoud	Egyptian	1138	Data&Agility	Testing	\N	\N	Machine Learning	ACTIVE	DevOps Engineer	Agility	11
140	\N	mahmoud.hassan202@company.com	\N	Male	4	2020-10-30	On-Payroll	t	Specialist	GS	KSA	Mahmoud Hassan	Egyptian	1139	OT	Agile	\N	\N	Agile	ACTIVE	Angular Engineer	Automation and Control	12
142	\N	dina.ali250@company.com	\N	Female	7	2022-09-11	On-Payroll	t	Specialist	GS	Egypt	Dina Ali	Egyptian	1141	EPIS	Machine Learning	\N	\N	Angular	ACTIVE	Angular Engineer	Cloud & Core Infrastructure Services	14
143	\N	sara.mohamed538@company.com	\N	Female	6	2023-08-28	On-Payroll	t	Specialist	GS	Egypt	Sara Mohamed	Egyptian	1142	EPIS	React	\N	\N	Machine Learning	ACTIVE	Project Management Engineer	Cloud & Core Infrastructure Services	15
144	\N	nour.el-sayed136@company.com	\N	Female	4	2025-12-27	On-Payroll	t	Specialist	GS	UAE	Nour El-Sayed	Egyptian	1143	Application	Python	\N	\N	React	ACTIVE	Project Management Engineer	Testing	16
145	\N	ali.farouk799@company.com	\N	Male	5	2021-11-22	On-Payroll	t	Specialist	GS	UAE	Ali Farouk	Egyptian	1144	Application	Cybersecurity	\N	\N	Data Engineering	ACTIVE	React Engineer	Testing	17
146	\N	nour.kamal205@company.com	\N	Female	7	2024-05-13	On-Payroll	t	Specialist	GS	KSA	Nour Kamal	Egyptian	1145	Data&Agility	React	\N	\N	Java	ACTIVE	Agile Engineer	Agility	18
147	\N	mohamed.ali510@company.com	\N	Male	5	2022-10-09	On-Payroll	t	Specialist	GS	UAE	Mohamed Ali	Egyptian	1146	Data&Agility	Cloud Services	\N	\N	Testing	ACTIVE	Data Engineering Engineer	Agility	19
150	\N	mohamed.ahmed229@company.com	\N	Male	7	2024-07-11	On-Payroll	t	Specialist	GS	KSA	Mohamed Ahmed	Egyptian	1149	EPIS	Data Engineering	\N	\N	Testing	ACTIVE	Java Engineer	Cloud & Core Infrastructure Services	6
152	\N	youssef.abdel317@company.com	\N	Male	6	2021-10-09	On-Payroll	t	Specialist	GS	UAE	Youssef Abdel	Egyptian	1151	Application	Cybersecurity	\N	\N	React	ACTIVE	Testing Engineer	Testing	8
153	\N	youssef.ibrahim530@company.com	\N	Male	3	2022-07-07	On-Payroll	t	Specialist	GS	Egypt	Youssef Ibrahim	Egyptian	1152	Application	Python	\N	\N	Project Management	ACTIVE	Business Analysis Engineer	Testing	9
154	\N	hana.mahmoud287@company.com	\N	Female	6	2022-04-07	On-Payroll	t	Specialist	GS	Egypt	Hana Mahmoud	Egyptian	1153	Data&Agility	Data Engineering	\N	\N	Testing	ACTIVE	Angular Engineer	Agility	10
155	\N	aisha.rashad274@company.com	\N	Female	C	2024-04-22	On-Payroll	t	Specialist	GS	Egypt	Aisha Rashad	Egyptian	1154	Data&Agility	Cloud Services	\N	\N	Cybersecurity	ACTIVE	Java Engineer	Agility	11
156	\N	hana.salem210@company.com	\N	Female	6	2025-10-07	On-Payroll	t	Specialist	GS	Egypt	Hana Salem	Egyptian	1155	OT	Business Analysis	\N	\N	Cloud Services	ACTIVE	Cloud Services Engineer	Automation and Control	12
157	\N	mahmoud.mahmoud118@company.com	\N	Male	6	2024-11-29	On-Payroll	t	Specialist	GS	KSA	Mahmoud Mahmoud	Egyptian	1156	OT	Business Analysis	\N	\N	DevOps	ACTIVE	Agile Engineer	Automation and Control	13
159	\N	hassan.el-sayed997@company.com	\N	Male	5	2024-06-09	On-Payroll	t	Specialist	GS	UAE	Hassan El-Sayed	Egyptian	1158	EPIS	Angular	\N	\N	Java	ACTIVE	Machine Learning Engineer	Cloud & Core Infrastructure Services	15
161	\N	ali.farouk366@company.com	\N	Male	6	2024-01-31	On-Payroll	t	Specialist	GS	Egypt	Ali Farouk	Egyptian	1160	Application	Network Engineering	\N	\N	Cloud Services	ACTIVE	Network Engineering Engineer	Testing	17
163	\N	ali.hassan416@company.com	\N	Male	C	2021-09-30	On-Payroll	t	Specialist	GS	Egypt	Ali Hassan	Egyptian	1162	Data&Agility	Python	\N	\N	Testing	ACTIVE	React Engineer	Agility	19
169	\N	khaled.mostafa216@company.com	\N	Male	4	2022-01-21	On-Payroll	t	Specialist	GS	Egypt	Khaled Mostafa	Egyptian	1168	Application	React	\N	\N	Angular	ACTIVE	Python Engineer	Testing	9
160	\N	khaled.mostafa145@company.com	\N	Male	4	2025-10-06	On-Payroll	t	Specialist	GS	Egypt	Khaled Mostafa	Egyptian	1159	Application	Java	\N	\N	Data Engineering	ACTIVE	Agile Engineer	Testing	16
168	\N	sara.mostafa85@company.com	\N	Female	4	2025-07-18	On-Payroll	t	Specialist	GS	KSA	Sara Mostafa	Egyptian	1167	Application	Java	\N	\N	Python	ACTIVE	Machine Learning Engineer	Testing	8
164	\N	khaled.farouk799@company.com	\N	Male	6	2022-09-19	On-Payroll	t	Specialist	GS	UAE	Khaled Farouk	Egyptian	1163	OT	Machine Learning	\N	\N	Business Analysis	ACTIVE	Cybersecurity Engineer	Automation and Control	20
151	\N	ali.hassan242@company.com	\N	Male	5	2021-11-27	On-Payroll	t	Specialist	GS	Egypt	Ali Hassan	Egyptian	1150	EPIS	Cloud Services	\N	\N	Testing	ACTIVE	DevOps Engineer	Cloud & Core Infrastructure Services	7
173	\N	dina.ahmed461@company.com	\N	Female	7	2023-11-09	On-Payroll	t	Specialist	GS	UAE	Dina Ahmed	Egyptian	1172	OT	Project Management	\N	\N	Project Management	ACTIVE	Java Engineer	Automation and Control	13
165	\N	mahmoud.farouk597@company.com	\N	Male	7	2021-06-24	On-Payroll	t	Specialist	GS	KSA	Mahmoud Farouk	Egyptian	1164	OT	Cloud Services	\N	\N	DevOps	ACTIVE	Testing Engineer	Automation and Control	21
171	\N	ali.rashad609@company.com	\N	Male	5	2025-12-13	On-Payroll	t	Specialist	GS	KSA	Ali Rashad	Egyptian	1170	Data&Agility	Business Analysis	\N	\N	Business Analysis	ACTIVE	Data Engineering Engineer	Agility	11
162	\N	aisha.mohamed288@company.com	\N	Female	5	2020-08-27	On-Payroll	t	Specialist	GS	Egypt	Aisha Mohamed	Egyptian	1161	Data&Agility	Java	\N	\N	Project Management	ACTIVE	Business Analysis Engineer	Agility	18
170	\N	khaled.ali177@company.com	\N	Male	6	2024-01-30	On-Payroll	t	Specialist	GS	KSA	Khaled Ali	Egyptian	1169	Data&Agility	Cybersecurity	\N	\N	Angular	ACTIVE	Data Engineering Engineer	Agility	10
172	\N	khaled.abdel814@company.com	\N	Male	4	2022-07-01	On-Payroll	t	Specialist	GS	Egypt	Khaled Abdel	Egyptian	1171	OT	Data Engineering	\N	\N	Data Engineering	LONG_LEAVE	Data Engineering Engineer	Automation and Control	12
166	\N	aisha.ahmed70@company.com	\N	Female	7	2023-03-27	On-Payroll	t	Specialist	GS	UAE	Aisha Ahmed	Egyptian	1165	EPIS	DevOps	\N	\N	DevOps	ACTIVE	Business Analysis Engineer	Cloud & Core Infrastructure Services	6
167	\N	dina.ahmed466@company.com	\N	Female	4	2022-10-11	On-Payroll	t	Specialist	GS	Egypt	Dina Ahmed	Egyptian	1166	EPIS	Project Management	\N	\N	Python	ACTIVE	Testing Engineer	Cloud & Core Infrastructure Services	7
148	\N	layla.ahmed242@company.com	\N	Female	7	2024-12-23	On-Payroll	t	Specialist	GS	KSA	Layla Ahmed	Egyptian	1147	OT	Agile	\N	\N	Testing	ACTIVE	Testing Engineer	Automation and Control	20
149	\N	fatma.kamal828@company.com	\N	Female	5	2023-06-17	On-Payroll	t	Specialist	GS	KSA	Fatma Kamal	Egyptian	1148	OT	DevOps	\N	\N	DevOps	ACTIVE	Python Engineer	Automation and Control	21
174	\N	hana.el-sayed127@company.com	\N	Female	6	2025-04-16	On-Payroll	t	Specialist	GS	UAE	Hana El-Sayed	Egyptian	1173	EPIS	DevOps	\N	\N	Agile	ACTIVE	Business Analysis Engineer	Cloud & Core Infrastructure Services	14
175	\N	marwa.hassan499@company.com	\N	Female	4	2021-01-18	On-Payroll	t	Specialist	GS	Egypt	Marwa Hassan	Egyptian	1174	EPIS	Data Engineering	\N	\N	Python	ACTIVE	Network Engineering Engineer	Cloud & Core Infrastructure Services	15
177	\N	youssef.abdel52@company.com	\N	Male	7	2023-03-11	On-Payroll	t	Specialist	GS	Egypt	Youssef Abdel	Egyptian	1176	Application	Cybersecurity	\N	\N	Business Analysis	ACTIVE	Project Management Engineer	Testing	17
178	\N	ali.hassan127@company.com	\N	Male	7	2025-02-17	On-Payroll	t	Specialist	GS	UAE	Ali Hassan	Egyptian	1177	Data&Agility	Cybersecurity	\N	\N	Cloud Services	ACTIVE	Project Management Engineer	Agility	18
179	\N	fatma.mohamed168@company.com	\N	Female	C	2023-09-19	On-Payroll	t	Specialist	GS	UAE	Fatma Mohamed	Egyptian	1178	Data&Agility	React	\N	\N	Java	ACTIVE	React Engineer	Agility	19
180	\N	youssef.salem558@company.com	\N	Male	6	2023-05-23	On-Payroll	t	Specialist	GS	Egypt	Youssef Salem	Egyptian	1179	OT	Python	\N	\N	Java	ACTIVE	Cybersecurity Engineer	Automation and Control	20
182	\N	mona.ali54@company.com	\N	Female	6	2024-08-15	On-Payroll	t	Specialist	GS	UAE	Mona Ali	Egyptian	1181	EPIS	Network Engineering	\N	\N	Machine Learning	ACTIVE	DevOps Engineer	Cloud & Core Infrastructure Services	6
183	\N	layla.ahmed188@company.com	\N	Female	7	2022-10-09	On-Payroll	t	Specialist	GS	Egypt	Layla Ahmed	Egyptian	1182	EPIS	Business Analysis	\N	\N	Testing	ACTIVE	Java Engineer	Cloud & Core Infrastructure Services	7
185	\N	khaled.mohamed103@company.com	\N	Male	4	2024-01-26	On-Payroll	t	Specialist	GS	KSA	Khaled Mohamed	Egyptian	1184	Application	Testing	\N	\N	Cloud Services	ACTIVE	Java Engineer	Testing	9
187	\N	khaled.abdel51@company.com	\N	Male	7	2021-02-14	On-Payroll	t	Specialist	GS	Egypt	Khaled Abdel	Egyptian	1186	Data&Agility	Testing	\N	\N	Angular	ACTIVE	Testing Engineer	Agility	11
196	\N	reem.farouk255@company.com	\N	Female	C	2023-07-18	On-Payroll	t	Specialist	GS	Egypt	Reem Farouk	Egyptian	1195	OT	Angular	\N	\N	Project Management	ACTIVE	Business Analysis Engineer	Automation and Control	20
205	\N	reem.farouk527@company.com	\N	Female	5	2024-01-24	On-Payroll	t	Specialist	GS	UAE	Reem Farouk	Egyptian	1204	OT	Business Analysis	\N	\N	Data Engineering	ACTIVE	DevOps Engineer	Automation and Control	13
206	\N	youssef.ibrahim804@company.com	\N	Male	7	2023-10-31	On-Payroll	t	Specialist	GS	UAE	Youssef Ibrahim	Egyptian	1205	EPIS	Network Engineering	\N	\N	Cybersecurity	ACTIVE	Machine Learning Engineer	Cloud & Core Infrastructure Services	14
201	\N	ali.rashad531@company.com	\N	Male	7	2022-01-18	On-Payroll	t	Specialist	GS	KSA	Ali Rashad	Egyptian	1200	Application	Angular	\N	\N	Cybersecurity	ACTIVE	React Engineer	Testing	9
198	\N	mahmoud.mohamed119@company.com	\N	Male	3	2021-10-06	On-Payroll	t	Specialist	GS	KSA	Mahmoud Mohamed	Egyptian	1197	EPIS	Data Engineering	\N	\N	Angular	ACTIVE	Python Engineer	Cloud & Core Infrastructure Services	6
200	\N	khaled.rashad898@company.com	\N	Male	4	2024-12-30	On-Payroll	t	Specialist	GS	UAE	Khaled Rashad	Egyptian	1199	Application	DevOps	\N	\N	Cybersecurity	ACTIVE	React Engineer	Testing	8
195	\N	dina.kamal343@company.com	\N	Female	4	2025-07-20	On-Payroll	t	Specialist	GS	KSA	Dina Kamal	Egyptian	1194	Data&Agility	Business Analysis	\N	\N	Agile	ACTIVE	Angular Engineer	Agility	19
181	\N	layla.salem108@company.com	\N	Female	3	2023-01-10	On-Payroll	t	Specialist	GS	Egypt	Layla Salem	Egyptian	1180	OT	Testing	\N	\N	Data Engineering	ACTIVE	Data Engineering Engineer	Automation and Control	21
197	\N	fatma.kamal246@company.com	\N	Female	6	2024-10-12	On-Payroll	t	Specialist	GS	Egypt	Fatma Kamal	Egyptian	1196	OT	Angular	\N	\N	Angular	ACTIVE	Machine Learning Engineer	Automation and Control	21
184	\N	mahmoud.ahmed905@company.com	\N	Male	5	2022-03-26	On-Payroll	t	Specialist	GS	UAE	Mahmoud Ahmed	Egyptian	1183	Application	Project Management	\N	\N	Java	ACTIVE	Data Engineering Engineer	Testing	8
199	\N	dina.mostafa306@company.com	\N	Female	5	2021-08-30	On-Payroll	t	Specialist	GS	UAE	Dina Mostafa	Egyptian	1198	EPIS	Python	\N	\N	React	ACTIVE	Java Engineer	Cloud & Core Infrastructure Services	7
186	\N	nour.ahmed526@company.com	\N	Female	4	2023-09-12	On-Payroll	t	Specialist	GS	KSA	Nour Ahmed	Egyptian	1185	Data&Agility	Machine Learning	\N	\N	Angular	ACTIVE	Cybersecurity Engineer	Agility	10
202	\N	mahmoud.mostafa416@company.com	\N	Male	5	2025-02-28	On-Payroll	t	Specialist	GS	Egypt	Mahmoud Mostafa	Egyptian	1201	Data&Agility	Cybersecurity	\N	\N	Network Engineering	ACTIVE	Java Engineer	Agility	10
188	\N	mohamed.ali635@company.com	\N	Male	6	2024-11-30	On-Payroll	t	Specialist	GS	KSA	Mohamed Ali	Egyptian	1187	OT	Business Analysis	\N	\N	Network Engineering	ACTIVE	Project Management Engineer	Automation and Control	12
204	\N	sara.hassan576@company.com	\N	Female	6	2021-05-31	On-Payroll	t	Specialist	GS	KSA	Sara Hassan	Egyptian	1203	OT	Data Engineering	\N	\N	Project Management	ACTIVE	Java Engineer	Automation and Control	12
190	\N	fatma.ibrahim141@company.com	\N	Female	4	2021-10-30	On-Payroll	t	Specialist	GS	KSA	Fatma Ibrahim	Egyptian	1189	EPIS	Project Management	\N	\N	Network Engineering	ACTIVE	DevOps Engineer	Cloud & Core Infrastructure Services	14
207	\N	mona.salem993@company.com	\N	Female	7	2023-05-03	On-Payroll	t	Specialist	GS	Egypt	Mona Salem	Egyptian	1206	EPIS	Angular	\N	\N	Cloud Services	ACTIVE	Network Engineering Engineer	Cloud & Core Infrastructure Services	15
191	\N	fatma.abdel988@company.com	\N	Female	6	2020-10-19	On-Payroll	t	Specialist	GS	Egypt	Fatma Abdel	Egyptian	1190	EPIS	Python	\N	\N	Business Analysis	ACTIVE	Java Engineer	Cloud & Core Infrastructure Services	15
192	\N	fatma.abdel382@company.com	\N	Female	6	2025-02-21	On-Payroll	t	Specialist	GS	Egypt	Fatma Abdel	Egyptian	1191	Application	Machine Learning	\N	\N	Testing	ACTIVE	Project Management Engineer	Testing	16
193	\N	ahmed.el-sayed299@company.com	\N	Male	3	2021-03-22	On-Payroll	t	Specialist	GS	Egypt	Ahmed El-Sayed	Egyptian	1192	Application	Angular	\N	\N	Java	ACTIVE	Testing Engineer	Testing	17
203	\N	dina.mohamed951@company.com	\N	Female	7	2020-11-21	On-Payroll	t	Specialist	GS	Egypt	Dina Mohamed	Egyptian	1202	Data&Agility	Java	\N	\N	Business Analysis	LONG_LEAVE	Machine Learning Engineer	Agility	11
194	\N	hassan.kamal542@company.com	\N	Male	5	2022-08-11	On-Payroll	t	Specialist	GS	UAE	Hassan Kamal	Egyptian	1193	Data&Agility	Network Engineering	\N	\N	Python	ACTIVE	Cybersecurity Engineer	Agility	18
189	\N	reem.nasser362@company.com	\N	Female	5	2022-01-19	On-Payroll	t	Specialist	GS	Egypt	Reem Nasser	Egyptian	1188	OT	Project Management	\N	\N	Project Management	MATERNITY	Python Engineer	Automation and Control	13
209	\N	ahmed.ali562@company.com	\N	Male	C	2022-06-16	On-Payroll	t	Specialist	GS	KSA	Ahmed Ali	Egyptian	1208	Application	Cloud Services	\N	\N	Machine Learning	ACTIVE	Cloud Services Engineer	Testing	17
210	\N	reem.salem855@company.com	\N	Female	7	2021-11-13	On-Payroll	t	Specialist	GS	UAE	Reem Salem	Egyptian	1209	Data&Agility	Java	\N	\N	Agile	ACTIVE	Angular Engineer	Agility	18
75	\N	youssef.farouk956@company.com	\N	Male	5	2023-12-24	On-Payroll	t	Specialist	GS	KSA	Youssef Farouk	Egyptian	1074	Data&Agility	Agile	\N	\N	Cloud Services	ACTIVE	Project Management Engineer	Agility	11
158	\N	nour.mohamed674@company.com	\N	Female	5	2025-01-24	On-Payroll	t	Specialist	GS	UAE	Nour Mohamed	Egyptian	1157	EPIS	React	\N	\N	Cybersecurity	ACTIVE	React Engineer	Cloud & Core Infrastructure Services	14
41	\N	aisha.nasser67@company.com	\N	Female	6	2024-12-25	On-Payroll	t	Specialist	GS	Egypt	Aisha Nasser	Egyptian	1040	Application	DevOps	\N	\N	Angular	ACTIVE	Project Management Engineer	Testing	9
211	\N	hana.salem309@company.com	\N	Female	C	2024-12-10	On-Payroll	t	Specialist	GS	UAE	Hana Salem	Egyptian	1210	Data&Agility	Business Analysis	\N	\N	Cloud Services	ACTIVE	Network Engineering Engineer	Agility	19
34	\N	hassan.ibrahim731@company.com	\N	Male	4	2025-08-11	On-Payroll	t	Specialist	GS	UAE	Hassan Ibrahim	Egyptian	1033	Data&Agility	DevOps	\N	\N	Python	ACTIVE	Machine Learning Engineer	Agility	18
208	\N	mohamed.ali632@company.com	\N	Male	3	2024-07-14	On-Payroll	t	Specialist	GS	KSA	Mohamed Ali	Egyptian	1207	Application	Python	\N	\N	Project Management	ACTIVE	Business Analysis Engineer	Testing	16
1	\N	ahmed.el-sayed130@company.com	\N	Male	N1	2020-09-17	On-Payroll	t	Executive	GS	UAE	Ahmed El-Sayed	Egyptian	1000	\N	DevOps	\N	\N	Data Engineering	ACTIVE	Chief Technology Officer	\N	\N
176	\N	mohamed.mahmoud803@company.com	\N	Male	5	2021-12-14	On-Payroll	t	Specialist	GS	UAE	Mohamed Mahmoud	Egyptian	1175	Application	Data Engineering	\N	\N	Cybersecurity	ACTIVE	Data Engineering Engineer	Testing	16
141	\N	dina.abdel78@company.com	\N	Female	6	2024-01-26	On-Payroll	t	Specialist	GS	UAE	Dina Abdel	Egyptian	1140	OT	Project Management	\N	\N	DevOps	MATERNITY	DevOps Engineer	Automation and Control	13
86	\N	khaled.mohamed431@company.com	\N	Male	4	2025-11-15	On-Payroll	t	Specialist	GS	KSA	Khaled Mohamed	Egyptian	1085	EPIS	Java	\N	\N	Data Engineering	ACTIVE	Data Engineering Engineer	Cloud & Core Infrastructure Services	6
126	\N	youssef.ahmed461@company.com	\N	Male	4	2025-12-08	On-Payroll	t	Specialist	GS	Egypt	Youssef Ahmed	Egyptian	1125	EPIS	Network Engineering	\N	\N	Agile	ACTIVE	Cybersecurity Engineer	Cloud & Core Infrastructure Services	14
61	\N	hassan.el-sayed281@company.com	\N	Male	7	2024-06-10	On-Payroll	t	Specialist	GS	Egypt	Hassan El-Sayed	Egyptian	1060	OT	Network Engineering	\N	\N	DevOps	RESIGNED	Business Analysis Engineer	Automation and Control	13
\.


--
-- Data for Name: monthly_allocations; Type: TABLE DATA; Schema: public; Owner: atlas
--

COPY public.monthly_allocations (id, allocation_id, year, month, percentage) FROM stdin;
73	9	2026	1	1
74	9	2026	2	0.75
75	9	2026	3	0.25
76	9	2026	4	0.25
77	9	2026	5	0.25
78	9	2026	6	1
79	9	2026	7	0.25
80	9	2026	8	1
81	9	2026	9	0.25
82	9	2026	10	0.25
83	9	2026	11	1
84	9	2026	12	0.25
97	11	2026	1	0.25
98	11	2026	2	0.5
99	11	2026	3	0.5
100	11	2026	4	0.25
101	11	2026	5	0.75
102	11	2026	6	0.5
103	11	2026	7	0.5
104	11	2026	8	1
105	11	2026	9	1
106	11	2026	10	1
107	11	2026	11	0.5
108	11	2026	12	0.75
121	13	2026	1	0.75
122	13	2026	2	0.5
123	13	2026	3	0.25
124	13	2026	4	0.75
125	13	2026	5	0.75
126	13	2026	6	1
127	13	2026	7	0.5
128	13	2026	8	0.5
129	13	2026	9	0.5
130	13	2026	10	0.75
131	13	2026	11	0.5
132	13	2026	12	0.25
145	16	2026	1	0.75
146	16	2026	2	1
147	16	2026	3	0.25
148	16	2026	4	1
149	16	2026	5	0.5
150	16	2026	6	0.75
151	16	2026	7	0.25
152	16	2026	8	0.75
153	16	2026	9	0.25
154	16	2026	10	0.25
155	16	2026	11	0.5
156	16	2026	12	0.25
181	19	2026	1	0.75
182	19	2026	2	0.25
183	19	2026	3	0.25
184	19	2026	4	0.5
185	19	2026	5	1
186	19	2026	6	0.75
187	19	2026	7	1
188	19	2026	8	0.25
189	19	2026	9	0.5
190	19	2026	10	0.75
191	19	2026	11	0.5
192	19	2026	12	0.75
217	23	2026	1	1
218	23	2026	2	0.5
219	23	2026	3	1
220	23	2026	4	1
221	23	2026	5	1
222	23	2026	6	0.5
223	23	2026	7	0.75
224	23	2026	8	0.25
225	23	2026	9	0.5
226	23	2026	10	0.5
227	23	2026	11	1
228	23	2026	12	1
265	27	2026	1	0.25
266	27	2026	2	0.75
267	27	2026	3	0.5
268	27	2026	4	0.75
269	27	2026	5	0.5
270	27	2026	6	1
271	27	2026	7	0.75
272	27	2026	8	0.5
273	27	2026	9	0.75
274	27	2026	10	0.25
275	27	2026	11	0.5
276	27	2026	12	0.5
277	28	2026	1	0.5
278	28	2026	2	1
279	28	2026	3	0.5
280	28	2026	4	0.25
281	28	2026	5	0.5
282	28	2026	6	0.5
283	28	2026	7	0.25
284	28	2026	8	0.5
285	28	2026	9	0.75
286	28	2026	10	0.5
287	28	2026	11	1
288	28	2026	12	0.5
313	32	2026	1	0.25
314	32	2026	2	0.75
315	32	2026	3	0.75
316	32	2026	4	0.5
317	32	2026	5	1
318	32	2026	6	1
319	32	2026	7	0.25
320	32	2026	8	0.5
321	32	2026	9	1
322	32	2026	10	0.25
323	32	2026	11	0.25
324	32	2026	12	0.5
325	33	2026	1	1
326	33	2026	2	1
327	33	2026	3	0.5
328	33	2026	4	0.25
329	33	2026	5	0.25
330	33	2026	6	1
331	33	2026	7	0.75
332	33	2026	8	0.25
333	33	2026	9	0.75
334	33	2026	10	0.25
335	33	2026	11	1
336	33	2026	12	0.5
373	38	2026	1	1
374	38	2026	2	0.5
375	38	2026	3	0.25
376	38	2026	4	0.5
377	38	2026	5	1
378	38	2026	6	0.75
379	38	2026	7	0.75
380	38	2026	8	0.25
381	38	2026	9	1
382	38	2026	10	1
383	38	2026	11	0.5
384	38	2026	12	0.5
385	39	2026	1	1
386	39	2026	2	0.25
387	39	2026	3	0.75
388	39	2026	4	0.5
389	39	2026	5	0.75
390	39	2026	6	0.5
391	39	2026	7	1
392	39	2026	8	0.75
393	39	2026	9	0.75
394	39	2026	10	0.5
395	39	2026	11	1
396	39	2026	12	1
445	45	2026	1	0.75
446	45	2026	2	0.5
447	45	2026	3	0.25
448	45	2026	4	0.75
449	45	2026	5	0.5
450	45	2026	6	1
451	45	2026	7	1
452	45	2026	8	0.75
453	45	2026	9	1
454	45	2026	10	0.5
455	45	2026	11	1
456	45	2026	12	0.75
457	46	2026	1	1
458	46	2026	2	0.5
459	46	2026	3	0.5
460	46	2026	4	0.25
461	46	2026	5	1
462	46	2026	6	0.75
463	46	2026	7	0.5
464	46	2026	8	0.5
465	46	2026	9	1
466	46	2026	10	0.25
467	46	2026	11	0.5
468	46	2026	12	0.5
481	48	2026	1	0.25
482	48	2026	2	1
483	48	2026	3	0.5
484	48	2026	4	0.5
485	48	2026	5	0.75
486	48	2026	6	0.75
487	48	2026	7	1
488	48	2026	8	0.25
489	48	2026	9	0.75
490	48	2026	10	0.75
491	48	2026	11	0.25
492	48	2026	12	1
529	53	2026	1	0.75
530	53	2026	2	0.25
531	53	2026	3	0.75
532	53	2026	4	0.75
533	53	2026	5	0.75
534	53	2026	6	1
535	53	2026	7	1
536	53	2026	8	0.75
537	53	2026	9	1
538	53	2026	10	1
539	53	2026	11	0.25
540	53	2026	12	0.5
541	54	2026	1	0.5
542	54	2026	2	0.25
543	54	2026	3	1
544	54	2026	4	1
545	54	2026	5	1
546	54	2026	6	0.25
547	54	2026	7	1
548	54	2026	8	0.75
549	54	2026	9	0.5
550	54	2026	10	0.75
551	54	2026	11	0.5
552	54	2026	12	1
565	56	2026	1	0.75
566	56	2026	2	0.25
567	56	2026	3	0.75
568	56	2026	4	0.75
569	56	2026	5	1
570	56	2026	6	1
571	56	2026	7	0.5
572	56	2026	8	0.5
573	56	2026	9	0.75
574	56	2026	10	0.25
575	56	2026	11	1
576	56	2026	12	0.75
577	58	2026	1	0.25
578	58	2026	2	0.25
579	58	2026	3	1
580	58	2026	4	0.25
581	58	2026	5	0.75
582	58	2026	6	1
583	58	2026	7	0.5
584	58	2026	8	0.5
585	58	2026	9	0.5
586	58	2026	10	1
587	58	2026	11	1
588	58	2026	12	1
661	66	2026	1	0.25
662	66	2026	2	0.25
663	66	2026	3	0.25
664	66	2026	4	0.75
665	66	2026	5	0.5
666	66	2026	6	0.25
667	66	2026	7	0.5
668	66	2026	8	0.25
669	66	2026	9	0.5
670	66	2026	10	0.75
671	66	2026	11	0.75
672	66	2026	12	0.25
685	68	2026	1	0.25
686	68	2026	2	0.75
687	68	2026	3	0.5
688	68	2026	4	0.75
689	68	2026	5	1
690	68	2026	6	1
691	68	2026	7	0.5
692	68	2026	8	0.5
693	68	2026	9	1
694	68	2026	10	0.75
695	68	2026	11	0.5
696	68	2026	12	0.5
709	70	2026	1	1
710	70	2026	2	0.5
711	70	2026	3	0.75
712	70	2026	4	0.75
713	70	2026	5	0.5
714	70	2026	6	0.75
715	70	2026	7	1
716	70	2026	8	0.5
717	70	2026	9	0.5
718	70	2026	10	1
719	70	2026	11	0.25
720	70	2026	12	0.25
733	73	2026	1	1
734	73	2026	2	0.25
735	73	2026	3	0.25
736	73	2026	4	0.5
737	73	2026	5	0.25
738	73	2026	6	1
739	73	2026	7	0.75
740	73	2026	8	1
741	73	2026	9	1
742	73	2026	10	0.5
743	73	2026	11	0.5
744	73	2026	12	0.5
769	76	2026	1	0.5
770	76	2026	2	1
771	76	2026	3	0.5
772	76	2026	4	1
773	76	2026	5	1
774	76	2026	6	0.5
775	76	2026	7	0.5
776	76	2026	8	0.5
777	76	2026	9	1
778	76	2026	10	0.75
779	76	2026	11	0.5
780	76	2026	12	0.25
805	80	2026	1	0.25
806	80	2026	2	1
807	80	2026	3	0.25
808	80	2026	4	0.75
809	80	2026	5	1
810	80	2026	6	0.75
811	80	2026	7	0.5
812	80	2026	8	0.5
813	80	2026	9	0.25
814	80	2026	10	0.25
815	80	2026	11	0.75
816	80	2026	12	1
853	84	2026	1	0.25
854	84	2026	2	0.25
855	84	2026	3	0.5
856	84	2026	4	0.25
857	84	2026	5	1
858	84	2026	6	0.75
859	84	2026	7	0.75
860	84	2026	8	0.25
861	84	2026	9	0.75
862	84	2026	10	1
863	84	2026	11	0.75
864	84	2026	12	0.25
865	86	2026	1	0.75
866	86	2026	2	1
867	86	2026	3	0.5
868	86	2026	4	0.5
869	86	2026	5	0.5
870	86	2026	6	1
871	86	2026	7	0.25
872	86	2026	8	0.75
873	86	2026	9	0.5
874	86	2026	10	0.75
875	86	2026	11	0.25
876	86	2026	12	1
901	89	2026	1	0.5
902	89	2026	2	0.25
903	89	2026	3	0.75
904	89	2026	4	0.75
905	89	2026	5	0.5
906	89	2026	6	1
907	89	2026	7	0.75
908	89	2026	8	1
909	89	2026	9	1
910	89	2026	10	1
911	89	2026	11	0.25
912	89	2026	12	0.25
913	90	2026	1	0.75
914	90	2026	2	1
915	90	2026	3	1
916	90	2026	4	0.25
917	90	2026	5	0.25
918	90	2026	6	0.75
919	90	2026	7	0.5
920	90	2026	8	0.25
921	90	2026	9	0.5
922	90	2026	10	0.25
923	90	2026	11	1
924	90	2026	12	0.5
961	95	2026	1	0.75
962	95	2026	2	0.25
963	95	2026	3	0.25
964	95	2026	4	1
965	95	2026	5	0.75
966	95	2026	6	1
967	95	2026	7	1
968	95	2026	8	0.5
969	95	2026	9	1
970	95	2026	10	0.5
971	95	2026	11	0.5
972	95	2026	12	0.5
973	96	2026	1	0.25
974	96	2026	2	0.75
975	96	2026	3	0.25
976	96	2026	4	0.25
977	96	2026	5	1
978	96	2026	6	0.75
979	96	2026	7	0.25
980	96	2026	8	0.75
981	96	2026	9	1
982	96	2026	10	0.5
983	96	2026	11	0.25
984	96	2026	12	0.5
1033	102	2026	1	0.25
1034	102	2026	2	0.5
1035	102	2026	3	0.75
1036	102	2026	4	0.25
1037	102	2026	5	0.75
1038	102	2026	6	0.5
1039	102	2026	7	0.75
1040	102	2026	8	0.25
1041	102	2026	9	0.5
1042	102	2026	10	0.25
1043	102	2026	11	0.75
1044	102	2026	12	0.25
1045	103	2026	1	0.25
1046	103	2026	2	1
1047	103	2026	3	0.25
1048	103	2026	4	0.25
1049	103	2026	5	0.75
1050	103	2026	6	0.75
1051	103	2026	7	0.25
1052	103	2026	8	0.25
1053	103	2026	9	1
1054	103	2026	10	0.5
1055	103	2026	11	1
1056	103	2026	12	0.5
1069	105	2026	1	0.75
1070	105	2026	2	0.25
1071	105	2026	3	0.75
1072	105	2026	4	0.25
1073	105	2026	5	0.75
1074	105	2026	6	0.5
1075	105	2026	7	0.75
1076	105	2026	8	0.5
1077	105	2026	9	0.25
1078	105	2026	10	0.75
1079	105	2026	11	0.5
1080	105	2026	12	0.75
1117	110	2026	1	1
1118	110	2026	2	1
1119	110	2026	3	1
1120	110	2026	4	0.25
1121	110	2026	5	0.25
1122	110	2026	6	0.25
1123	110	2026	7	1
1124	110	2026	8	0.75
1125	110	2026	9	0.75
1126	110	2026	10	0.75
1127	110	2026	11	0.5
1128	110	2026	12	0.75
1129	111	2026	1	0.75
1130	111	2026	2	0.25
1131	111	2026	3	1
1132	111	2026	4	1
1133	111	2026	5	0.75
1134	111	2026	6	1
1135	111	2026	7	0.5
1136	111	2026	8	0.5
1137	111	2026	9	0.75
1138	111	2026	10	1
1139	111	2026	11	0.25
1140	111	2026	12	0.25
1141	112	2026	1	1
1142	112	2026	2	0.25
1143	112	2026	3	0.75
1144	112	2026	4	0.75
1145	112	2026	5	0.25
1146	112	2026	6	0.5
1147	112	2026	7	1
1148	112	2026	8	0.75
1149	112	2026	9	0.75
1150	112	2026	10	0.5
1151	112	2026	11	0.5
1152	112	2026	12	0.5
1165	115	2026	1	0.5
1166	115	2026	2	1
1167	115	2026	3	0.25
1168	115	2026	4	0.75
1169	115	2026	5	0.25
1170	115	2026	6	0.25
1171	115	2026	7	1
1172	115	2026	8	0.25
1173	115	2026	9	0.5
1174	115	2026	10	0.75
1175	115	2026	11	1
1176	115	2026	12	1
1249	123	2026	1	0.5
1250	123	2026	2	0.5
1251	123	2026	3	0.75
1252	123	2026	4	0.75
1253	123	2026	5	0.25
1254	123	2026	6	0.25
1255	123	2026	7	0.25
1256	123	2026	8	0.75
1257	123	2026	9	0.75
1258	123	2026	10	0.25
1259	123	2026	11	0.5
1260	123	2026	12	0.25
1261	124	2026	1	1
1262	124	2026	2	0.25
1263	124	2026	3	1
1264	124	2026	4	0.5
1265	124	2026	5	0.5
1266	124	2026	6	0.75
1267	124	2026	7	0.5
1268	124	2026	8	1
1269	124	2026	9	1
1270	124	2026	10	0.25
1271	124	2026	11	0.75
1272	124	2026	12	0.25
1285	126	2026	1	0.25
1286	126	2026	2	0.25
1287	126	2026	3	0.75
1288	126	2026	4	0.25
1289	126	2026	5	0.25
1290	126	2026	6	1
1291	126	2026	7	0.5
1292	126	2026	8	0.5
1293	126	2026	9	1
1294	126	2026	10	0.25
1295	126	2026	11	0.5
1296	126	2026	12	1
1309	129	2026	1	1
1310	129	2026	2	0.5
1311	129	2026	3	1
1312	129	2026	4	1
1313	129	2026	5	1
1314	129	2026	6	1
1315	129	2026	7	1
1316	129	2026	8	0.75
1317	129	2026	9	0.75
1318	129	2026	10	0.75
1319	129	2026	11	1
1320	129	2026	12	0.75
1345	132	2026	1	0.75
1346	132	2026	2	0.25
1347	132	2026	3	1
1348	132	2026	4	0.5
1349	132	2026	5	1
1350	132	2026	6	1
1351	132	2026	7	0.5
1352	132	2026	8	0.5
1353	132	2026	9	0.25
1354	132	2026	10	1
1355	132	2026	11	0.25
1356	132	2026	12	1
1381	136	2026	1	0.5
1382	136	2026	2	1
1383	136	2026	3	0.5
1384	136	2026	4	0.5
1385	136	2026	5	0.25
1386	136	2026	6	0.75
1387	136	2026	7	0.5
1388	136	2026	8	0.5
1389	136	2026	9	0.5
1390	136	2026	10	0.75
1391	136	2026	11	0.75
1392	136	2026	12	0.25
1429	140	2026	1	0.5
1430	140	2026	2	0.25
1431	140	2026	3	0.75
1432	140	2026	4	0.75
1433	140	2026	5	0.75
1434	140	2026	6	0.5
1435	140	2026	7	1
1436	140	2026	8	0.25
1437	140	2026	9	0.25
1438	140	2026	10	1
1439	140	2026	11	0.25
1440	140	2026	12	1
1441	142	2026	1	0.5
1442	142	2026	2	0.75
1443	142	2026	3	0.75
1444	142	2026	4	0.5
1445	142	2026	5	0.25
1446	142	2026	6	0.5
1447	142	2026	7	0.75
1448	142	2026	8	0.5
1449	142	2026	9	0.75
1450	142	2026	10	1
1451	142	2026	11	1
1452	142	2026	12	0.5
1477	145	2026	1	0.75
1478	145	2026	2	0.25
1479	145	2026	3	1
1480	145	2026	4	0.25
1481	145	2026	5	0.5
1482	145	2026	6	0.25
1483	145	2026	7	1
1484	145	2026	8	1
1485	145	2026	9	0.75
1486	145	2026	10	1
1487	145	2026	11	1
1488	145	2026	12	1
1501	147	2026	1	0.5
1502	147	2026	2	0.5
1503	147	2026	3	0.5
1504	147	2026	4	0.5
1505	147	2026	5	0.75
1506	147	2026	6	1
1507	147	2026	7	0.75
1508	147	2026	8	0.25
1509	147	2026	9	0.5
1510	147	2026	10	0.75
1511	147	2026	11	1
1512	147	2026	12	0.75
1537	151	2026	1	0.75
1538	151	2026	2	0.75
1539	151	2026	3	0.75
1540	151	2026	4	0.5
1541	151	2026	5	1
1542	151	2026	6	0.5
1543	151	2026	7	0.25
1544	151	2026	8	0.25
1545	151	2026	9	0.5
1546	151	2026	10	0.75
1547	151	2026	11	0.5
1548	151	2026	12	0.25
1549	152	2026	1	0.75
1550	152	2026	2	1
1551	152	2026	3	0.75
1552	152	2026	4	0.5
1553	152	2026	5	0.5
1554	152	2026	6	0.25
1555	152	2026	7	0.5
1556	152	2026	8	0.75
1557	152	2026	9	0.25
1558	152	2026	10	0.75
1559	152	2026	11	0.75
1560	152	2026	12	1
1609	158	2026	1	0.5
1610	158	2026	2	0.5
1611	158	2026	3	0.25
1612	158	2026	4	0.25
1613	158	2026	5	1
1614	158	2026	6	0.25
1615	158	2026	7	0.25
1616	158	2026	8	0.25
1617	158	2026	9	0.25
1618	158	2026	10	1
1619	158	2026	11	0.75
1620	158	2026	12	0.75
1621	159	2026	1	0.75
1622	159	2026	2	0.75
1623	159	2026	3	0.5
1624	159	2026	4	0.5
1625	159	2026	5	0.25
1626	159	2026	6	0.75
1627	159	2026	7	1
1628	159	2026	8	0.25
1629	159	2026	9	0.5
1630	159	2026	10	0.5
1631	159	2026	11	1
1632	159	2026	12	0.75
1645	161	2026	1	1
1646	161	2026	2	0.25
1647	161	2026	3	0.75
1648	161	2026	4	1
1649	161	2026	5	0.75
1650	161	2026	6	1
1651	161	2026	7	1
1652	161	2026	8	0.5
1653	161	2026	9	1
1654	161	2026	10	0.25
1655	161	2026	11	0.75
1656	161	2026	12	0.75
1693	166	2026	1	0.75
1694	166	2026	2	1
1695	166	2026	3	1
1696	166	2026	4	0.75
1697	166	2026	5	1
1698	166	2026	6	1
1699	166	2026	7	0.25
1700	166	2026	8	0.5
1701	166	2026	9	1
1702	166	2026	10	0.25
1703	166	2026	11	0.5
1704	166	2026	12	0.25
1705	167	2026	1	1
1706	167	2026	2	1
1707	167	2026	3	0.25
1708	167	2026	4	0.75
1709	167	2026	5	0.75
1710	167	2026	6	1
1711	167	2026	7	1
1712	167	2026	8	0.25
1713	167	2026	9	1
1714	167	2026	10	0.25
1715	167	2026	11	0.75
1716	167	2026	12	1
1717	168	2026	1	0.75
1718	168	2026	2	1
1719	168	2026	3	0.25
1720	168	2026	4	1
1721	168	2026	5	0.25
1722	168	2026	6	0.75
1723	168	2026	7	0.25
1724	168	2026	8	1
1725	168	2026	9	0.75
1726	168	2026	10	0.25
1727	168	2026	11	0.25
1728	168	2026	12	0.25
1729	170	2026	1	0.5
1730	170	2026	2	0.5
1731	170	2026	3	0.75
1732	170	2026	4	0.5
1733	170	2026	5	0.25
1734	170	2026	6	1
1735	170	2026	7	0.75
1736	170	2026	8	0.75
1737	170	2026	9	0.5
1738	170	2026	10	1
1739	170	2026	11	0.75
1740	170	2026	12	0.25
1753	172	2026	1	0.5
1754	172	2026	2	0.75
1755	172	2026	3	1
1756	172	2026	4	1
1757	172	2026	5	0.5
1758	172	2026	6	0.5
1759	172	2026	7	0.75
1760	172	2026	8	0.5
1761	172	2026	9	0.5
1762	172	2026	10	0.75
1763	172	2026	11	0.25
1764	172	2026	12	0.5
1837	180	2026	1	0.5
1838	180	2026	2	1
1839	180	2026	3	0.5
1840	180	2026	4	0.75
1841	180	2026	5	1
1842	180	2026	6	0.5
1843	180	2026	7	0.5
1844	180	2026	8	0.5
1845	180	2026	9	1
1846	180	2026	10	0.75
1847	180	2026	11	0.5
1848	180	2026	12	0.75
1849	181	2026	1	1
1850	181	2026	2	0.5
1851	181	2026	3	0.25
1852	181	2026	4	1
1853	181	2026	5	1
1854	181	2026	6	1
1855	181	2026	7	0.25
1856	181	2026	8	0.25
1857	181	2026	9	0.25
1858	181	2026	10	1
1859	181	2026	11	0.75
1860	181	2026	12	0.75
1873	184	2026	1	0.25
1874	184	2026	2	0.25
1875	184	2026	3	0.75
1876	184	2026	4	1
1877	184	2026	5	0.25
1878	184	2026	6	0.25
1879	184	2026	7	0.25
1880	184	2026	8	1
1881	184	2026	9	0.5
1882	184	2026	10	0.75
1883	184	2026	11	1
1884	184	2026	12	0.25
1897	186	2026	1	0.25
1898	186	2026	2	0.75
1899	186	2026	3	1
1900	186	2026	4	1
1901	186	2026	5	0.5
1902	186	2026	6	0.5
1903	186	2026	7	1
1904	186	2026	8	0.5
1905	186	2026	9	0.5
1906	186	2026	10	0.25
1907	186	2026	11	0.25
1908	186	2026	12	0.75
1933	189	2026	1	0.25
1934	189	2026	2	0.25
1935	189	2026	3	0.25
1936	189	2026	4	0.25
1937	189	2026	5	0.25
1938	189	2026	6	0.5
1939	189	2026	7	0.75
1940	189	2026	8	0.75
1941	189	2026	9	1
1942	189	2026	10	0.75
1943	189	2026	11	0.5
1944	189	2026	12	0.75
1969	193	2026	1	0.25
1970	193	2026	2	0.75
1971	193	2026	3	0.75
1972	193	2026	4	1
1973	193	2026	5	0.5
1974	193	2026	6	1
1975	193	2026	7	0.5
1976	193	2026	8	1
1977	193	2026	9	0.25
1978	193	2026	10	0.5
1979	193	2026	11	0.25
1980	193	2026	12	0.75
2017	198	2026	1	1
2018	198	2026	2	0.5
2019	198	2026	3	0.75
2020	198	2026	4	0.75
2021	198	2026	5	0.5
2022	198	2026	6	0.75
2023	198	2026	7	0.75
2024	198	2026	8	0.25
2025	198	2026	9	1
2026	198	2026	10	1
2027	198	2026	11	0.75
2028	198	2026	12	0.25
2029	199	2026	1	0.25
2030	199	2026	2	1
2031	199	2026	3	1
2032	199	2026	4	0.75
2033	199	2026	5	0.75
2034	199	2026	6	0.25
2035	199	2026	7	0.25
2036	199	2026	8	1
2037	199	2026	9	0.5
2038	199	2026	10	0.75
2039	199	2026	11	0.5
2040	199	2026	12	1
2065	202	2026	1	0.25
2066	202	2026	2	0.5
2067	202	2026	3	0.75
2068	202	2026	4	0.75
2069	202	2026	5	1
2070	202	2026	6	0.5
2071	202	2026	7	0.75
2072	202	2026	8	1
2073	202	2026	9	0.25
2074	202	2026	10	0.25
2075	202	2026	11	0.25
2076	202	2026	12	0.25
2089	205	2026	1	1
2090	205	2026	2	0.75
2091	205	2026	3	0.5
2092	205	2026	4	1
2093	205	2026	5	0.25
2094	205	2026	6	0.75
2095	205	2026	7	1
2096	205	2026	8	0.5
2097	205	2026	9	1
2098	205	2026	10	0.25
2099	205	2026	11	1
2100	205	2026	12	0.25
2125	208	2026	1	1
2126	208	2026	2	1
2127	208	2026	3	1
2128	208	2026	4	0.75
2129	208	2026	5	0.5
2130	208	2026	6	0.25
2131	208	2026	7	0.75
2132	208	2026	8	1
2133	208	2026	9	0.25
2134	208	2026	10	0.25
2135	208	2026	11	0.75
2136	208	2026	12	0.75
2137	209	2026	1	1
2138	209	2026	2	0.5
2139	209	2026	3	0.75
2140	209	2026	4	0.5
2141	209	2026	5	1
2142	209	2026	6	1
2143	209	2026	7	0.75
2144	209	2026	8	0.75
2145	209	2026	9	0.5
2146	209	2026	10	0.5
2147	209	2026	11	0.75
2148	209	2026	12	0.75
2197	215	2026	1	0.5
2198	215	2026	2	1
2199	215	2026	3	0.25
2200	215	2026	4	0.5
2201	215	2026	5	0.5
2202	215	2026	6	0.75
2203	215	2026	7	0.25
2204	215	2026	8	0.25
2205	215	2026	9	0.75
2206	215	2026	10	0.25
2207	215	2026	11	1
2208	215	2026	12	1
2209	216	2026	1	1
2210	216	2026	2	0.75
2211	216	2026	3	1
2212	216	2026	4	0.25
2213	216	2026	5	1
2214	216	2026	6	0.75
2215	216	2026	7	0.5
2216	216	2026	8	0.5
2217	216	2026	9	0.5
2218	216	2026	10	0.25
2219	216	2026	11	0.25
2220	216	2026	12	0.75
2233	219	2026	1	0.5
2234	219	2026	2	1
2235	219	2026	3	0.5
2236	219	2026	4	0.5
2237	219	2026	5	0.75
2238	219	2026	6	0.5
2239	219	2026	7	0.75
2240	219	2026	8	1
2241	219	2026	9	0.25
2242	219	2026	10	0.5
2243	219	2026	11	0.25
2244	219	2026	12	0.5
2281	223	2026	1	0.25
2282	223	2026	2	0.25
2283	223	2026	3	0.25
2284	223	2026	4	1
2285	223	2026	5	0.25
2286	223	2026	6	1
2287	223	2026	7	1
2288	223	2026	8	1
2289	223	2026	9	0.75
2290	223	2026	10	0.25
2291	223	2026	11	0.25
2292	223	2026	12	0.75
2293	224	2026	1	0.25
2294	224	2026	2	1
2295	224	2026	3	0.75
2296	224	2026	4	0.25
2297	224	2026	5	0.5
2298	224	2026	6	0.25
2299	224	2026	7	0.5
2300	224	2026	8	1
2301	224	2026	9	0.25
2302	224	2026	10	1
2303	224	2026	11	0.25
2304	224	2026	12	0.25
2305	226	2026	1	0.25
2306	226	2026	2	0.25
2307	226	2026	3	0.5
2308	226	2026	4	0.5
2309	226	2026	5	0.25
2310	226	2026	6	0.5
2311	226	2026	7	1
2312	226	2026	8	1
2313	226	2026	9	0.5
2314	226	2026	10	0.25
2315	226	2026	11	0.5
2316	226	2026	12	0.5
2317	227	2026	1	0.75
2318	227	2026	2	1
2319	227	2026	3	1
2320	227	2026	4	0.25
2321	227	2026	5	0.5
2322	227	2026	6	0.25
2323	227	2026	7	1
2324	227	2026	8	1
2325	227	2026	9	0.25
2326	227	2026	10	0.75
2327	227	2026	11	1
2328	227	2026	12	1
2341	229	2026	1	0.5
2342	229	2026	2	0.75
2343	229	2026	3	0.75
2344	229	2026	4	0.25
2345	229	2026	5	0.75
2346	229	2026	6	0.25
2347	229	2026	7	0.5
2348	229	2026	8	0.5
2349	229	2026	9	0.5
2350	229	2026	10	0.75
2351	229	2026	11	0.5
2352	229	2026	12	0.5
2425	237	2026	1	0.25
2426	237	2026	2	1
2427	237	2026	3	1
2428	237	2026	4	0.75
2429	237	2026	5	0.75
2430	237	2026	6	0.25
2431	237	2026	7	0.25
2432	237	2026	8	0.25
2433	237	2026	9	0.25
2434	237	2026	10	0.5
2435	237	2026	11	0.25
2436	237	2026	12	0.25
2437	238	2026	1	0.75
2438	238	2026	2	0.25
2439	238	2026	3	0.5
2440	238	2026	4	1
2441	238	2026	5	0.5
2442	238	2026	6	0.5
2443	238	2026	7	0.25
2444	238	2026	8	0.75
2445	238	2026	9	1
2446	238	2026	10	0.25
2447	238	2026	11	0.5
2448	238	2026	12	0.25
2461	241	2026	1	0.5
2462	241	2026	2	0.25
2463	241	2026	3	0.5
2464	241	2026	4	0.25
2465	241	2026	5	0.25
2466	241	2026	6	0.5
2467	241	2026	7	0.5
2468	241	2026	8	0.5
2469	241	2026	9	0.75
2470	241	2026	10	0.75
2471	241	2026	11	0.25
2472	241	2026	12	0.25
2485	243	2026	1	0.5
2486	243	2026	2	1
2487	243	2026	3	0.5
2488	243	2026	4	0.75
2489	243	2026	5	0.25
2490	243	2026	6	0.5
2491	243	2026	7	0.25
2492	243	2026	8	0.75
2493	243	2026	9	1
2494	243	2026	10	0.75
2495	243	2026	11	1
2496	243	2026	12	1
2521	247	2026	1	1
2522	247	2026	2	0.5
2523	247	2026	3	0.5
2524	247	2026	4	0.75
2525	247	2026	5	1
2526	247	2026	6	0.75
2527	247	2026	7	0.5
2528	247	2026	8	1
2529	247	2026	9	0.75
2530	247	2026	10	0.5
2531	247	2026	11	1
2532	247	2026	12	0.75
2557	250	2026	1	0.75
2558	250	2026	2	0.5
2559	250	2026	3	0.25
2560	250	2026	4	0.75
2561	250	2026	5	0.75
2562	250	2026	6	1
2563	250	2026	7	0.75
2564	250	2026	8	0.25
2565	250	2026	9	1
2566	250	2026	10	1
2567	250	2026	11	0.75
2568	250	2026	12	0.75
2605	255	2026	1	0.75
2606	255	2026	2	1
2607	255	2026	3	1
2608	255	2026	4	0.25
2609	255	2026	5	0.5
2610	255	2026	6	0.25
2611	255	2026	7	0.5
2612	255	2026	8	0.75
2613	255	2026	9	1
2614	255	2026	10	0.75
2615	255	2026	11	0.25
2616	255	2026	12	0.25
2617	256	2026	1	0.5
2618	256	2026	2	0.5
2619	256	2026	3	0.75
2620	256	2026	4	1
2621	256	2026	5	0.5
2622	256	2026	6	0.75
2623	256	2026	7	0.75
2624	256	2026	8	1
2625	256	2026	9	0.75
2626	256	2026	10	0.5
2627	256	2026	11	1
2628	256	2026	12	1
2653	259	2026	1	0.75
2654	259	2026	2	0.75
2655	259	2026	3	0.25
2656	259	2026	4	0.25
2657	259	2026	5	0.75
2658	259	2026	6	0.5
2659	259	2026	7	0.5
2660	259	2026	8	0.75
2661	259	2026	9	1
2662	259	2026	10	1
2663	259	2026	11	0.5
2664	259	2026	12	0.75
2677	262	2026	1	0.75
2678	262	2026	2	1
2679	262	2026	3	1
2680	262	2026	4	1
2681	262	2026	5	0.25
2682	262	2026	6	0.75
2683	262	2026	7	0.25
2684	262	2026	8	1
2685	262	2026	9	0.75
2686	262	2026	10	0.75
2687	262	2026	11	0.25
2688	262	2026	12	0.25
2713	265	2026	1	0.75
2714	265	2026	2	0.5
2715	265	2026	3	0.75
2716	265	2026	4	0.5
2717	265	2026	5	0.25
2718	265	2026	6	1
2719	265	2026	7	0.5
2720	265	2026	8	1
2721	265	2026	9	0.75
2722	265	2026	10	0.5
2723	265	2026	11	0.25
2724	265	2026	12	0.75
2725	266	2026	1	1
2726	266	2026	2	0.5
2727	266	2026	3	0.5
2728	266	2026	4	0.75
2729	266	2026	5	0.75
2730	266	2026	6	0.5
2731	266	2026	7	1
2732	266	2026	8	0.75
2733	266	2026	9	1
2734	266	2026	10	0.25
2735	266	2026	11	0.25
2736	266	2026	12	0.5
2785	272	2026	1	0.75
2786	272	2026	2	0.75
2787	272	2026	3	0.5
2788	272	2026	4	0.25
2789	272	2026	5	1
2790	272	2026	6	0.5
2791	272	2026	7	0.75
2792	272	2026	8	0.5
2793	272	2026	9	0.25
2794	272	2026	10	0.5
2795	272	2026	11	0.25
2796	272	2026	12	1
2797	273	2026	1	0.5
2798	273	2026	2	1
2799	273	2026	3	1
2800	273	2026	4	0.25
2801	273	2026	5	0.25
2802	273	2026	6	0.5
2803	273	2026	7	0.75
2804	273	2026	8	1
2805	273	2026	9	0.75
2806	273	2026	10	0.25
2807	273	2026	11	0.75
2808	273	2026	12	0.5
2821	276	2026	1	0.5
2822	276	2026	2	0.75
2823	276	2026	3	0.25
2824	276	2026	4	0.75
2825	276	2026	5	1
2826	276	2026	6	0.5
2827	276	2026	7	0.5
2828	276	2026	8	1
2829	276	2026	9	1
2830	276	2026	10	0.25
2831	276	2026	11	0.75
2832	276	2026	12	1
\.


--
-- Data for Name: projects; Type: TABLE DATA; Schema: public; Owner: atlas
--

COPY public.projects (id, description, end_date, name, parent_tower, project_id, start_date, status, tower, manager_id) FROM stdin;
3	Project for Infrastructure Upgrade	2026-07-08	Infrastructure Upgrade	EPIS	PRJ-1002	2025-03-08	ON_HOLD	Cloud & Core Infrastructure Services	6
9	Project for Data Lake	2026-05-08	Data Lake	Data&Agility	PRJ-1008	2025-07-08	COMPLETED	Agility	10
2	Project for Network Security	2026-03-08	Network Security	EPIS	PRJ-1001	2025-11-08	COMPLETED	Cloud & Core Infrastructure Services	7
12	Project for ML Pipeline	2026-03-08	ML Pipeline	Data&Agility	PRJ-1011	2025-03-08	ON_HOLD	Agility	11
14	Project for IoT Platform	2026-03-08	IoT Platform	OT	PRJ-1013	2026-02-08	COMPLETED	Automation and Control	13
16	Project for Industrial Automation	2026-02-08	Industrial Automation	OT	PRJ-1015	2025-10-08	ON_HOLD	Automation and Control	13
5	Project for Digital Transformation	2026-04-08	Digital Transformation	Application	PRJ-1004	2025-09-08	COMPLETED	Testing	8
8	Project for ERP Integration	2026-08-08	ERP Integration	Application	PRJ-1007	2025-12-08	ON_HOLD	Testing	9
6	Project for Mobile App	2027-01-08	Mobile App	Application	PRJ-1005	2025-04-08	COMPLETED	Testing	9
11	Project for Analytics Dashboard	2026-11-08	Analytics Dashboard	Data&Agility	PRJ-1010	2026-02-08	ON_HOLD	Agility	10
1	Project for Cloud Migration	2026-06-08	Cloud Migration	EPIS	PRJ-1000	2025-03-08	COMPLETED	Cloud & Core Infrastructure Services	6
7	Project for Customer Portal	2026-10-08	Customer Portal	Application	PRJ-1006	2025-12-08	ON_HOLD	Testing	8
15	Project for SCADA Modernization	2026-12-08	SCADA Modernization	OT	PRJ-1014	2025-11-08	COMPLETED	Automation and Control	12
4	Project for Cybersecurity Platform	2026-11-08	Cybersecurity Platform	EPIS	PRJ-1003	2025-05-08	COMPLETED	Cloud & Core Infrastructure Services	7
10	Project for AI Platform	2026-08-08	AI Platform	Data&Agility	PRJ-1009	2025-07-08	ON_HOLD	Agility	11
13	Project for Smart Meters	2026-04-08	Smart Meters	OT	PRJ-1012	2026-01-08	COMPLETED	Automation and Control	12
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: atlas
--

COPY public.users (id, email, is_active, manager_level, password, role, username, employee_id) FROM stdin;
1	admin@company.com	t	0	$2a$10$4XGwovTRjCF1gqDFerKIxOlof077NzMieOihROf27DtXj00p6uxXO	SYSTEM_ADMIN	admin	\N
2	ahmed.el-sayed130@company.com	t	1	$2a$10$FsjQpiv74c0mSpa9a9Tc5uc4kRu6UUdIdxGIyflUaavh9F2rmF0My	EXECUTIVE	ahmed.el-sayed	1
3	mohamed.hassan525@company.com	t	2	$2a$10$0u8bvzY0UqMATKE6VXaebuFp5NAD1R8Op6EOfEUuBae1EnwfMvNaC	HEAD	mohamed.hassan	2
4	sara.ibrahim182@company.com	t	2	$2a$10$voI2HPBX7/PIfzxW1taDsO7kyGr60PUPxzBkzojFunsXOm3RbR3Ra	HEAD	sara.ibrahim	3
5	khaled.nasser32@company.com	t	2	$2a$10$zE9pH2O9iEezKkFqO4sFN.7wcDitvQ/U8KFDpSDpdS4.zCurYZ8nu	HEAD	khaled.nasser	4
6	hassan.farouk400@company.com	t	2	$2a$10$LvNDJcPrOv7FNwx99.EFF.3aMikb/giJuE90iSQRRBaWfGgUlau6.	HEAD	hassan.farouk	5
7	omar.mohamed241@company.com	t	3	$2a$10$xTvSIl0sP7pC56X9OWhmQeWfUtmcckjqktU73g6i0082QozZ2OGsW	DEPARTMENT_MANAGER	omar.mohamed	6
8	ali.mahmoud730@company.com	t	3	$2a$10$gXhCo3mHOxXvUmmijtz5supF5A6M6goZGHMWFWeSbhgvQDJUHEJmu	DEPARTMENT_MANAGER	ali.mahmoud	7
9	marwa.ali727@company.com	t	3	$2a$10$mU7wj8SFg/KZLaKjnS6z7eIn8wZaIiQPGXFSpEO51AvuZklvMClwa	DEPARTMENT_MANAGER	marwa.ali	8
10	dina.salem379@company.com	t	3	$2a$10$o25UfzkEqKxvyi4ueOWcGOPqqJXf6V7S5sAynOSUfwMTpXJTUTZZ6	DEPARTMENT_MANAGER	dina.salem	9
11	youssef.kamal534@company.com	t	3	$2a$10$ScnIyYeMDlXXDZtaOqEoB.w.ImntPxWmyQJ1SuA2ZPZrzLdk.wVy6	DEPARTMENT_MANAGER	youssef.kamal	10
12	reem.rashad193@company.com	t	3	$2a$10$.cjHtSYtHaWSnPqhH.dvOeCXpf8QEDX8w/Nbq9iUCbevYe0deZj7W	DEPARTMENT_MANAGER	reem.rashad	11
13	mahmoud.ibrahim410@company.com	t	3	$2a$10$eGv9knmD79lwWiQ40SSSbeLm6Jb1u.iXkTaTXL81zdpqiIDJjc3Ny	DEPARTMENT_MANAGER	mahmoud.ibrahim	12
14	layla.hassan897@company.com	t	3	$2a$10$..a4zoBM2zKr3uExHbt1me22KmkuGZWtK5yydYIUajht.q8ms/JPO	DEPARTMENT_MANAGER	layla.hassan	13
15	nour.mostafa794@company.com	t	4	$2a$10$nr2s6eVvBVUMn3vNqJM7zO256wnmOLonXGKRSPIQi.CjuHYpLssEO	TEAM_LEAD	nour.mostafa	14
16	fatma.ahmed390@company.com	t	4	$2a$10$K90/p5wd.b/SUV40sjHfAOFkK685i67vf9SSC9fdnptfWgwQA90Za	TEAM_LEAD	fatma.ahmed	15
17	hana.mohamed733@company.com	t	4	$2a$10$A2w2ZvVg7g.0HthUVRdbWuwAedJ2bfykTpn3rLDQm370CXJtQvMnq	TEAM_LEAD	hana.mohamed	16
18	aisha.ali200@company.com	t	4	$2a$10$JAXnKJkBYrSQNQsaqDUmYOsc4g//tCjFRRrKf..LqAdgNHZkAOUKm	TEAM_LEAD	aisha.ali	17
19	mona.hassan593@company.com	t	4	$2a$10$e4Asl5VJ9b9/Xv1ylqzUauG2xNGNxTeJFDMGoiKUF.tjLoZfFIj7m	TEAM_LEAD	mona.hassan	18
20	ahmed.abdel747@company.com	t	4	$2a$10$3xWgW9MUNi8PoJqdPejPtuuwXXyXnL0xT7niTYXfxUR3gtNaU3rXW	TEAM_LEAD	ahmed.abdel	19
21	mohamed.kamal823@company.com	t	4	$2a$10$C2lZBA8yCyXMDoEiC7.KfuStLvtKPtAbNvO9JLjZ7sZypSaG6K//i	TEAM_LEAD	mohamed.kamal	20
22	omar.salem870@company.com	t	4	$2a$10$MHIaNAilvvFa.n12IbLXi.zNPq8etg56P6IxEYhkjQzp27xkUe/0K	TEAM_LEAD	omar.salem	21
\.


--
-- Name: allocations_id_seq; Type: SEQUENCE SET; Schema: public; Owner: atlas
--

SELECT pg_catalog.setval('public.allocations_id_seq', 277, true);


--
-- Name: employees_id_seq; Type: SEQUENCE SET; Schema: public; Owner: atlas
--

SELECT pg_catalog.setval('public.employees_id_seq', 211, true);


--
-- Name: monthly_allocations_id_seq; Type: SEQUENCE SET; Schema: public; Owner: atlas
--

SELECT pg_catalog.setval('public.monthly_allocations_id_seq', 7824, true);


--
-- Name: projects_id_seq; Type: SEQUENCE SET; Schema: public; Owner: atlas
--

SELECT pg_catalog.setval('public.projects_id_seq', 16, true);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: atlas
--

SELECT pg_catalog.setval('public.users_id_seq', 22, true);


--
-- Name: allocations allocations_pkey; Type: CONSTRAINT; Schema: public; Owner: atlas
--

ALTER TABLE ONLY public.allocations
    ADD CONSTRAINT allocations_pkey PRIMARY KEY (id);


--
-- Name: employees employees_pkey; Type: CONSTRAINT; Schema: public; Owner: atlas
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT employees_pkey PRIMARY KEY (id);


--
-- Name: monthly_allocations monthly_allocations_pkey; Type: CONSTRAINT; Schema: public; Owner: atlas
--

ALTER TABLE ONLY public.monthly_allocations
    ADD CONSTRAINT monthly_allocations_pkey PRIMARY KEY (id);


--
-- Name: projects projects_pkey; Type: CONSTRAINT; Schema: public; Owner: atlas
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT projects_pkey PRIMARY KEY (id);


--
-- Name: users uk_6dotkott2kjsp8vw4d0m25fb7; Type: CONSTRAINT; Schema: public; Owner: atlas
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);


--
-- Name: employees uk_7046dnihcwphbs2tcujqqs5b7; Type: CONSTRAINT; Schema: public; Owner: atlas
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT uk_7046dnihcwphbs2tcujqqs5b7 UNIQUE (oracle_id);


--
-- Name: monthly_allocations uk_alloc_year_month; Type: CONSTRAINT; Schema: public; Owner: atlas
--

ALTER TABLE ONLY public.monthly_allocations
    ADD CONSTRAINT uk_alloc_year_month UNIQUE (allocation_id, year, month);


--
-- Name: users uk_d1s31g1a7ilra77m65xmka3ei; Type: CONSTRAINT; Schema: public; Owner: atlas
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_d1s31g1a7ilra77m65xmka3ei UNIQUE (employee_id);


--
-- Name: employees uk_j9xgmd0ya5jmus09o0b8pqrpb; Type: CONSTRAINT; Schema: public; Owner: atlas
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT uk_j9xgmd0ya5jmus09o0b8pqrpb UNIQUE (email);


--
-- Name: users uk_r43af9ap4edm43mmtq01oddj6; Type: CONSTRAINT; Schema: public; Owner: atlas
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_r43af9ap4edm43mmtq01oddj6 UNIQUE (username);


--
-- Name: projects uk_r4sng6mj7mni8wyteu3g52kbr; Type: CONSTRAINT; Schema: public; Owner: atlas
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT uk_r4sng6mj7mni8wyteu3g52kbr UNIQUE (project_id);


--
-- Name: monthly_allocations ukjlp4c7ch2253194diftkj5jj1; Type: CONSTRAINT; Schema: public; Owner: atlas
--

ALTER TABLE ONLY public.monthly_allocations
    ADD CONSTRAINT ukjlp4c7ch2253194diftkj5jj1 UNIQUE (allocation_id, year, month);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: atlas
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: users fk6p2ib82uai0pj9yk1iassppgq; Type: FK CONSTRAINT; Schema: public; Owner: atlas
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fk6p2ib82uai0pj9yk1iassppgq FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: monthly_allocations fk_monthly_allocation; Type: FK CONSTRAINT; Schema: public; Owner: atlas
--

ALTER TABLE ONLY public.monthly_allocations
    ADD CONSTRAINT fk_monthly_allocation FOREIGN KEY (allocation_id) REFERENCES public.allocations(id) ON DELETE CASCADE;


--
-- Name: employees fki4365uo9af35g7jtbc2rteukt; Type: FK CONSTRAINT; Schema: public; Owner: atlas
--

ALTER TABLE ONLY public.employees
    ADD CONSTRAINT fki4365uo9af35g7jtbc2rteukt FOREIGN KEY (manager_id) REFERENCES public.employees(id);


--
-- Name: allocations fklgyvi1ifhphug0govaqwnsqyf; Type: FK CONSTRAINT; Schema: public; Owner: atlas
--

ALTER TABLE ONLY public.allocations
    ADD CONSTRAINT fklgyvi1ifhphug0govaqwnsqyf FOREIGN KEY (project_id) REFERENCES public.projects(id);


--
-- Name: allocations fklidedpimhddtisgg66kca1to7; Type: FK CONSTRAINT; Schema: public; Owner: atlas
--

ALTER TABLE ONLY public.allocations
    ADD CONSTRAINT fklidedpimhddtisgg66kca1to7 FOREIGN KEY (employee_id) REFERENCES public.employees(id);


--
-- Name: projects fksg57tut2cx77vmci14sy4vbsu; Type: FK CONSTRAINT; Schema: public; Owner: atlas
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT fksg57tut2cx77vmci14sy4vbsu FOREIGN KEY (manager_id) REFERENCES public.employees(id);


--
-- PostgreSQL database dump complete
--

\unrestrict m5QX1iBaLigryipKQFhEixUg7jhSKfYv8Yh6iINDB4FFw9QPRex0O8d3HPX3Lmo

