package com.pooja.jobportal.config;

import com.pooja.jobportal.model.*;
import com.pooja.jobportal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        boolean hasUsers     = userRepository.findByEmail("admin@jobportal.com").isPresent();
        boolean hasCompanies = companyRepository.count() > 0;
        boolean hasJobs      = jobRepository.count() > 0;

        // Nothing at all — full seed
        if (!hasUsers && !hasCompanies && !hasJobs) {
            seedUsers();
            seedCompanies();
            seedJobs();
            seedApplications();
            printCredentials();
            return;
        }

        // Partial state: admin inserted but companies/jobs missing (common after a failed first run)
        if (hasUsers && !hasCompanies) {
            System.out.println("[DataSeeder] Users exist but companies/jobs missing — seeding missing data.");
            seedUsers();   // idempotent — skips existing emails
            seedCompanies();
            seedJobs();
            seedApplications();
            printCredentials();
            return;
        }

        if (hasUsers && hasCompanies && !hasJobs) {
            System.out.println("[DataSeeder] Companies exist but jobs missing — seeding jobs and applications.");
            seedJobs();
            seedApplications();
            printCredentials();
            return;
        }

        System.out.println("[DataSeeder] Seed data already present — skipping.");
    }

    private List<User> seedUsers() {
        String pw = passwordEncoder.encode("Password@123");

        User admin   = saveUserIfAbsent("admin@jobportal.com",   User.builder().name("Admin User").email("admin@jobportal.com").password(passwordEncoder.encode("Admin@123")).role(Role.ADMIN).build());
        User alice   = saveUserIfAbsent("alice@example.com",     User.builder().name("Alice Johnson").email("alice@example.com").password(pw).role(Role.JOB_SEEKER).phone("+1-555-0101").skills("Java, Spring Boot, PostgreSQL, REST APIs").build());
        User bob     = saveUserIfAbsent("bob@example.com",       User.builder().name("Bob Smith").email("bob@example.com").password(pw).role(Role.JOB_SEEKER).phone("+1-555-0102").skills("React, TypeScript, Node.js, CSS").build());
        User charlie = saveUserIfAbsent("charlie@example.com",   User.builder().name("Charlie Davis").email("charlie@example.com").password(pw).role(Role.JOB_SEEKER).phone("+1-555-0103").skills("Python, Machine Learning, TensorFlow, Data Analysis").build());
        User diana   = saveUserIfAbsent("diana@example.com",     User.builder().name("Diana Prince").email("diana@example.com").password(pw).role(Role.JOB_SEEKER).phone("+1-555-0104").skills("DevOps, Docker, Kubernetes, AWS, CI/CD").build());
        User eve     = saveUserIfAbsent("eve@example.com",       User.builder().name("Eve Martinez").email("eve@example.com").password(pw).role(Role.JOB_SEEKER).phone("+1-555-0105").skills("UI/UX Design, Figma, Adobe XD, HTML, CSS").build());

        return Arrays.asList(admin, alice, bob, charlie, diana, eve);
    }

    private User saveUserIfAbsent(String email, User candidate) {
        return userRepository.findByEmail(email).orElseGet(() -> userRepository.save(candidate));
    }

    private List<Company> seedCompanies() {
        String pw = passwordEncoder.encode("Company@123");

        Company techcorp = companyRepository.save(Company.builder()
                .name("TechCorp Solutions")
                .email("hr@techcorp.com")
                .password(pw)
                .industry("Information Technology")
                .companySize("501-1000")
                .website("https://techcorp.com")
                .description("TechCorp Solutions is a leading software company specializing in enterprise-grade SaaS products. We build scalable solutions for Fortune 500 clients worldwide.")
                .logoUrl("https://ui-avatars.com/api/?name=TechCorp+Solutions&background=4F46E5&color=fff&size=128")
                .verificationStatus(CompanyVerificationStatus.VERIFIED)
                .build());

        Company innovate = companyRepository.save(Company.builder()
                .name("InnovateSoft")
                .email("careers@innovatesoft.io")
                .password(pw)
                .industry("Software Development")
                .companySize("51-200")
                .website("https://innovatesoft.io")
                .description("InnovateSoft is a fast-growing startup focused on building next-generation developer tools and productivity software. We value creativity, speed, and impact.")
                .logoUrl("https://ui-avatars.com/api/?name=InnovateSoft&background=10B981&color=fff&size=128")
                .verificationStatus(CompanyVerificationStatus.VERIFIED)
                .build());

        Company datadriven = companyRepository.save(Company.builder()
                .name("DataDriven Inc")
                .email("jobs@datadriven.ai")
                .password(pw)
                .industry("Data & Analytics")
                .companySize("201-500")
                .website("https://datadriven.ai")
                .description("DataDriven Inc helps businesses harness the power of AI and machine learning. Our platform processes billions of data points daily to deliver actionable insights.")
                .logoUrl("https://ui-avatars.com/api/?name=DataDriven+Inc&background=F59E0B&color=fff&size=128")
                .verificationStatus(CompanyVerificationStatus.VERIFIED)
                .build());

        Company cloudnine = companyRepository.save(Company.builder()
                .name("CloudNine Systems")
                .email("talent@cloudnine.dev")
                .password(pw)
                .industry("Cloud Computing")
                .companySize("11-50")
                .website("https://cloudnine.dev")
                .description("CloudNine Systems is a cloud infrastructure startup helping SMBs migrate to the cloud. Founded in 2021, we are growing rapidly and looking for passionate engineers.")
                .logoUrl("https://ui-avatars.com/api/?name=CloudNine+Systems&background=3B82F6&color=fff&size=128")
                .verificationStatus(CompanyVerificationStatus.PENDING)
                .build());

        Company greenbridge = companyRepository.save(Company.builder()
                .name("GreenBridge Tech")
                .email("work@greenbridge.co")
                .password(pw)
                .industry("CleanTech")
                .companySize("201-500")
                .website("https://greenbridge.co")
                .description("GreenBridge Tech builds sustainability software for enterprises. Our platform tracks carbon footprint, manages ESG reporting, and drives green initiatives.")
                .logoUrl("https://ui-avatars.com/api/?name=GreenBridge+Tech&background=22C55E&color=fff&size=128")
                .verificationStatus(CompanyVerificationStatus.VERIFIED)
                .build());

        return Arrays.asList(techcorp, innovate, datadriven, cloudnine, greenbridge);
    }

    private void seedJobs() {
        Company techcorp   = companyRepository.findByEmail("hr@techcorp.com").orElseThrow();
        Company innovate   = companyRepository.findByEmail("careers@innovatesoft.io").orElseThrow();
        Company datadriven = companyRepository.findByEmail("jobs@datadriven.ai").orElseThrow();
        Company cloudnine  = companyRepository.findByEmail("talent@cloudnine.dev").orElseThrow();
        Company greenbridge = companyRepository.findByEmail("work@greenbridge.co").orElseThrow();

        LocalDate soon = LocalDate.now().plusDays(30);
        LocalDate later = LocalDate.now().plusDays(60);
        LocalDate distant = LocalDate.now().plusDays(90);

        // TechCorp jobs
        jobRepository.save(Job.builder()
                .company(techcorp).title("Senior Java Backend Engineer").slug("senior-java-backend-engineer-techcorp")
                .type(JobType.FULL_TIME).workplaceType(WorkplaceType.HYBRID).experienceLevel(ExperienceLevel.SENIOR)
                .status(JobStatus.PUBLISHED).isActive(true)
                .salary(Salary.builder().min(110000.0).max(145000.0).currency("USD").period(Salary.SalaryPeriod.YEARLY).isNegotiable(false).build())
                .location(Location.builder().city("San Francisco").state("CA").country("USA").zipCode("94105").build())
                .skills(Arrays.asList("Java", "Spring Boot", "PostgreSQL", "Microservices", "AWS"))
                .description("<p>We are looking for a Senior Java Backend Engineer to join our core platform team. You will design and build high-throughput microservices serving millions of requests per day.</p>")
                .responsibilities("<ul><li>Design and implement scalable microservices</li><li>Own end-to-end delivery of backend features</li><li>Mentor junior engineers</li><li>Collaborate with product and infrastructure teams</li></ul>")
                .requirements("<ul><li>5+ years of Java experience</li><li>Strong knowledge of Spring Boot and JPA</li><li>Experience with AWS services</li><li>Familiarity with CI/CD pipelines</li></ul>")
                .benefits("<ul><li>Competitive salary + equity</li><li>Full health, dental, vision</li><li>Flexible PTO</li><li>$2000 annual learning budget</li></ul>")
                .deadline(later).build());

        jobRepository.save(Job.builder()
                .company(techcorp).title("Frontend React Developer").slug("frontend-react-developer-techcorp")
                .type(JobType.FULL_TIME).workplaceType(WorkplaceType.REMOTE).experienceLevel(ExperienceLevel.MID)
                .status(JobStatus.PUBLISHED).isActive(true)
                .salary(Salary.builder().min(85000.0).max(115000.0).currency("USD").period(Salary.SalaryPeriod.YEARLY).isNegotiable(true).build())
                .location(Location.builder().city("Remote").state("").country("USA").zipCode("").build())
                .skills(Arrays.asList("React", "TypeScript", "Redux", "REST APIs", "Jest"))
                .description("<p>Join TechCorp as a Frontend React Developer and build beautiful, performant UIs used by thousands of enterprise customers.</p>")
                .responsibilities("<ul><li>Build and maintain React component library</li><li>Implement pixel-perfect UI from Figma designs</li><li>Write unit and integration tests</li><li>Optimize web performance</li></ul>")
                .requirements("<ul><li>3+ years of React experience</li><li>Proficiency in TypeScript</li><li>Experience with state management (Redux/Zustand)</li><li>Strong CSS skills</li></ul>")
                .benefits("<ul><li>100% remote</li><li>Home office stipend ($1500)</li><li>Health insurance</li><li>Stock options</li></ul>")
                .deadline(soon).build());

        jobRepository.save(Job.builder()
                .company(techcorp).title("DevOps Engineer").slug("devops-engineer-techcorp")
                .type(JobType.FULL_TIME).workplaceType(WorkplaceType.ONSITE).experienceLevel(ExperienceLevel.MID)
                .status(JobStatus.PUBLISHED).isActive(true)
                .salary(Salary.builder().min(95000.0).max(125000.0).currency("USD").period(Salary.SalaryPeriod.YEARLY).isNegotiable(false).build())
                .location(Location.builder().city("Seattle").state("WA").country("USA").zipCode("98101").build())
                .skills(Arrays.asList("Docker", "Kubernetes", "AWS", "Terraform", "CI/CD", "Jenkins"))
                .description("<p>TechCorp is seeking a DevOps Engineer to strengthen our infrastructure team. You will manage our Kubernetes clusters and drive automation initiatives.</p>")
                .responsibilities("<ul><li>Manage AWS infrastructure with Terraform</li><li>Maintain Kubernetes clusters</li><li>Build CI/CD pipelines</li><li>Monitor system reliability and performance</li></ul>")
                .requirements("<ul><li>3+ years of DevOps/infrastructure experience</li><li>Strong Kubernetes knowledge</li><li>AWS certifications preferred</li><li>Scripting in Bash/Python</li></ul>")
                .benefits("<ul><li>Relocation assistance</li><li>401k with 4% match</li><li>Gym membership</li><li>Annual AWS certification reimbursement</li></ul>")
                .deadline(later).build());

        jobRepository.save(Job.builder()
                .company(techcorp).title("QA Engineer Intern").slug("qa-engineer-intern-techcorp")
                .type(JobType.INTERNSHIP).workplaceType(WorkplaceType.HYBRID).experienceLevel(ExperienceLevel.JUNIOR)
                .status(JobStatus.PUBLISHED).isActive(true)
                .salary(Salary.builder().min(25.0).max(35.0).currency("USD").period(Salary.SalaryPeriod.HOURLY).isNegotiable(false).build())
                .location(Location.builder().city("San Francisco").state("CA").country("USA").zipCode("94105").build())
                .skills(Arrays.asList("Selenium", "JUnit", "Manual Testing", "JIRA", "Agile"))
                .description("<p>Great internship opportunity to learn QA engineering at scale. You will work alongside senior QA engineers and contribute to test automation frameworks.</p>")
                .responsibilities("<ul><li>Write and execute manual test cases</li><li>Assist in building Selenium test scripts</li><li>Report and track bugs in JIRA</li><li>Participate in sprint ceremonies</li></ul>")
                .requirements("<ul><li>Currently pursuing CS or related degree</li><li>Basic programming knowledge</li><li>Keen eye for detail</li></ul>")
                .benefits("<ul><li>Competitive hourly pay</li><li>Mentorship program</li><li>Return offer consideration</li></ul>")
                .deadline(soon).build());

        // InnovateSoft jobs
        jobRepository.save(Job.builder()
                .company(innovate).title("Full Stack Engineer").slug("full-stack-engineer-innovatesoft")
                .type(JobType.FULL_TIME).workplaceType(WorkplaceType.REMOTE).experienceLevel(ExperienceLevel.MID)
                .status(JobStatus.PUBLISHED).isActive(true)
                .salary(Salary.builder().min(90000.0).max(120000.0).currency("USD").period(Salary.SalaryPeriod.YEARLY).isNegotiable(true).build())
                .location(Location.builder().city("Remote").state("").country("USA").zipCode("").build())
                .skills(Arrays.asList("Node.js", "React", "MongoDB", "GraphQL", "Docker"))
                .description("<p>InnovateSoft is hiring a Full Stack Engineer to build and scale our developer productivity platform. You will have end-to-end ownership of features.</p>")
                .responsibilities("<ul><li>Build full-stack features from design to deployment</li><li>Design GraphQL schemas and resolvers</li><li>Optimize database queries</li><li>Participate in product discussions</li></ul>")
                .requirements("<ul><li>3+ years of full-stack experience</li><li>Proficiency in Node.js and React</li><li>Experience with GraphQL</li><li>MongoDB or similar NoSQL experience</li></ul>")
                .benefits("<ul><li>Remote-first culture</li><li>Equity package</li><li>Unlimited PTO</li><li>Annual retreat</li></ul>")
                .deadline(later).build());

        jobRepository.save(Job.builder()
                .company(innovate).title("Product Designer").slug("product-designer-innovatesoft")
                .type(JobType.FULL_TIME).workplaceType(WorkplaceType.REMOTE).experienceLevel(ExperienceLevel.MID)
                .status(JobStatus.PUBLISHED).isActive(true)
                .salary(Salary.builder().min(80000.0).max(105000.0).currency("USD").period(Salary.SalaryPeriod.YEARLY).isNegotiable(true).build())
                .location(Location.builder().city("Remote").state("").country("USA").zipCode("").build())
                .skills(Arrays.asList("Figma", "UI/UX Design", "Prototyping", "User Research", "Design Systems"))
                .description("<p>We are looking for a Product Designer to craft delightful user experiences for our developer tools. You will work directly with the CEO and engineering leads.</p>")
                .responsibilities("<ul><li>Design wireframes and high-fidelity prototypes</li><li>Build and maintain our design system</li><li>Conduct user interviews and usability tests</li><li>Collaborate closely with engineering</li></ul>")
                .requirements("<ul><li>3+ years of product design experience</li><li>Expert-level Figma skills</li><li>Portfolio showcasing B2B SaaS products</li><li>Experience with design systems</li></ul>")
                .benefits("<ul><li>Remote work</li><li>Design tool budget</li><li>Health benefits</li><li>Early-stage equity</li></ul>")
                .deadline(distant).build());

        jobRepository.save(Job.builder()
                .company(innovate).title("Backend Engineer (Part-time)").slug("backend-engineer-parttime-innovatesoft")
                .type(JobType.PART_TIME).workplaceType(WorkplaceType.REMOTE).experienceLevel(ExperienceLevel.JUNIOR)
                .status(JobStatus.PUBLISHED).isActive(true)
                .salary(Salary.builder().min(40.0).max(60.0).currency("USD").period(Salary.SalaryPeriod.HOURLY).isNegotiable(false).build())
                .location(Location.builder().city("Remote").state("").country("USA").zipCode("").build())
                .skills(Arrays.asList("Python", "FastAPI", "PostgreSQL", "REST APIs"))
                .description("<p>Part-time role for a backend engineer to help build our API platform. Ideal for someone in school or with another commitment who wants flexible hours.</p>")
                .responsibilities("<ul><li>Build and maintain REST APIs with FastAPI</li><li>Write database migrations</li><li>Review pull requests</li></ul>")
                .requirements("<ul><li>1+ years of Python backend experience</li><li>PostgreSQL knowledge</li><li>Available 20 hours/week</li></ul>")
                .benefits("<ul><li>Flexible schedule</li><li>Remote work</li><li>Potential for full-time conversion</li></ul>")
                .deadline(soon).build());

        // DataDriven jobs
        jobRepository.save(Job.builder()
                .company(datadriven).title("Machine Learning Engineer").slug("ml-engineer-datadriven")
                .type(JobType.FULL_TIME).workplaceType(WorkplaceType.HYBRID).experienceLevel(ExperienceLevel.SENIOR)
                .status(JobStatus.PUBLISHED).isActive(true)
                .salary(Salary.builder().min(130000.0).max(175000.0).currency("USD").period(Salary.SalaryPeriod.YEARLY).isNegotiable(false).build())
                .location(Location.builder().city("New York").state("NY").country("USA").zipCode("10001").build())
                .skills(Arrays.asList("Python", "TensorFlow", "PyTorch", "MLOps", "Spark", "Kubernetes"))
                .description("<p>DataDriven Inc is seeking an experienced ML Engineer to join our AI Platform team. You will design, train, and deploy ML models that power our core analytics products.</p>")
                .responsibilities("<ul><li>Design and train production-grade ML models</li><li>Build MLOps pipelines for model deployment</li><li>Collaborate with data scientists on feature engineering</li><li>Optimize model inference latency</li></ul>")
                .requirements("<ul><li>5+ years of ML engineering experience</li><li>Strong Python and TensorFlow/PyTorch skills</li><li>Experience with MLOps tools (MLflow, Kubeflow)</li><li>MS/PhD in CS, ML, or related field preferred</li></ul>")
                .benefits("<ul><li>Top-tier compensation + equity</li><li>Research budget</li><li>Conference attendance</li><li>Comprehensive health benefits</li></ul>")
                .deadline(distant).build());

        jobRepository.save(Job.builder()
                .company(datadriven).title("Data Analyst").slug("data-analyst-datadriven")
                .type(JobType.FULL_TIME).workplaceType(WorkplaceType.ONSITE).experienceLevel(ExperienceLevel.JUNIOR)
                .status(JobStatus.PUBLISHED).isActive(true)
                .salary(Salary.builder().min(65000.0).max(85000.0).currency("USD").period(Salary.SalaryPeriod.YEARLY).isNegotiable(false).build())
                .location(Location.builder().city("New York").state("NY").country("USA").zipCode("10001").build())
                .skills(Arrays.asList("SQL", "Python", "Tableau", "Excel", "Statistical Analysis"))
                .description("<p>Join DataDriven's analytics team to help clients understand their data. You will create dashboards, run analyses, and present findings to stakeholders.</p>")
                .responsibilities("<ul><li>Build and maintain Tableau dashboards</li><li>Write complex SQL queries for data extraction</li><li>Prepare weekly and monthly reports</li><li>Collaborate with client success teams</li></ul>")
                .requirements("<ul><li>1-2 years of data analysis experience</li><li>Strong SQL skills</li><li>Proficiency in Tableau or Power BI</li><li>Bachelor's degree in relevant field</li></ul>")
                .benefits("<ul><li>Training and certification support</li><li>Health and dental insurance</li><li>Performance bonus</li></ul>")
                .deadline(later).build());

        jobRepository.save(Job.builder()
                .company(datadriven).title("Data Science Intern").slug("data-science-intern-datadriven")
                .type(JobType.INTERNSHIP).workplaceType(WorkplaceType.HYBRID).experienceLevel(ExperienceLevel.JUNIOR)
                .status(JobStatus.PUBLISHED).isActive(true)
                .salary(Salary.builder().min(30.0).max(40.0).currency("USD").period(Salary.SalaryPeriod.HOURLY).isNegotiable(false).build())
                .location(Location.builder().city("New York").state("NY").country("USA").zipCode("10001").build())
                .skills(Arrays.asList("Python", "Pandas", "Scikit-learn", "Jupyter", "Statistics"))
                .description("<p>Summer internship for aspiring data scientists. Work on real-world datasets and contribute to production models under the guidance of senior data scientists.</p>")
                .responsibilities("<ul><li>Explore and clean datasets</li><li>Build baseline ML models</li><li>Present findings in weekly team meetings</li></ul>")
                .requirements("<ul><li>Pursuing BS/MS in Data Science, CS, or Statistics</li><li>Python and pandas experience</li><li>Basic statistics knowledge</li></ul>")
                .benefits("<ul><li>Paid internship</li><li>Mentorship</li><li>Full-time offer opportunity</li></ul>")
                .deadline(soon).build());

        // CloudNine jobs
        jobRepository.save(Job.builder()
                .company(cloudnine).title("Cloud Infrastructure Engineer").slug("cloud-infra-engineer-cloudnine")
                .type(JobType.FULL_TIME).workplaceType(WorkplaceType.REMOTE).experienceLevel(ExperienceLevel.MID)
                .status(JobStatus.PUBLISHED).isActive(true)
                .salary(Salary.builder().min(100000.0).max(130000.0).currency("USD").period(Salary.SalaryPeriod.YEARLY).isNegotiable(true).build())
                .location(Location.builder().city("Remote").state("").country("USA").zipCode("").build())
                .skills(Arrays.asList("AWS", "Terraform", "Ansible", "Python", "Linux", "Networking"))
                .description("<p>CloudNine Systems is growing and needs an Infrastructure Engineer to help our clients migrate to AWS. You'll be hands-on with cloud architecture and automation.</p>")
                .responsibilities("<ul><li>Design AWS architectures for client migrations</li><li>Write Terraform modules for infrastructure-as-code</li><li>Automate provisioning with Ansible</li><li>Provide technical support during migrations</li></ul>")
                .requirements("<ul><li>3+ years of AWS experience</li><li>Strong Terraform knowledge</li><li>AWS Solutions Architect certification preferred</li><li>Excellent communication skills</li></ul>")
                .benefits("<ul><li>Fully remote</li><li>AWS certification sponsorship</li><li>Profit sharing</li><li>Flexible hours</li></ul>")
                .deadline(later).build());

        jobRepository.save(Job.builder()
                .company(cloudnine).title("Sales Engineer").slug("sales-engineer-cloudnine")
                .type(JobType.FULL_TIME).workplaceType(WorkplaceType.REMOTE).experienceLevel(ExperienceLevel.MID)
                .status(JobStatus.PUBLISHED).isActive(true)
                .salary(Salary.builder().min(90000.0).max(120000.0).currency("USD").period(Salary.SalaryPeriod.YEARLY).isNegotiable(true).build())
                .location(Location.builder().city("Remote").state("").country("USA").zipCode("").build())
                .skills(Arrays.asList("Cloud Computing", "AWS", "Technical Sales", "Communication", "Solution Architecture"))
                .description("<p>We need a Sales Engineer to bridge the gap between our technical team and prospective customers. You'll run demos, answer RFPs, and help close enterprise deals.</p>")
                .responsibilities("<ul><li>Run live product demonstrations</li><li>Answer technical questions during the sales cycle</li><li>Build proof-of-concept environments for prospects</li><li>Work closely with account executives</li></ul>")
                .requirements("<ul><li>2+ years in a technical sales or sales engineering role</li><li>Strong AWS knowledge</li><li>Excellent presentation skills</li><li>Customer-facing experience</li></ul>")
                .benefits("<ul><li>Remote work</li><li>Commission structure</li><li>Health benefits</li><li>Startup equity</li></ul>")
                .deadline(distant).build());

        // GreenBridge jobs
        jobRepository.save(Job.builder()
                .company(greenbridge).title("Lead Software Engineer").slug("lead-software-engineer-greenbridge")
                .type(JobType.FULL_TIME).workplaceType(WorkplaceType.HYBRID).experienceLevel(ExperienceLevel.LEAD)
                .status(JobStatus.PUBLISHED).isActive(true)
                .salary(Salary.builder().min(140000.0).max(180000.0).currency("USD").period(Salary.SalaryPeriod.YEARLY).isNegotiable(false).build())
                .location(Location.builder().city("Austin").state("TX").country("USA").zipCode("78701").build())
                .skills(Arrays.asList("Java", "Spring Boot", "React", "PostgreSQL", "AWS", "System Design"))
                .description("<p>GreenBridge Tech is hiring a Lead Software Engineer to drive the technical vision of our sustainability platform. You'll lead a team of 5 engineers and shape our architecture.</p>")
                .responsibilities("<ul><li>Lead technical design and architecture decisions</li><li>Mentor and grow a team of engineers</li><li>Deliver quarterly product roadmap goals</li><li>Collaborate with CTO on technical strategy</li></ul>")
                .requirements("<ul><li>7+ years of software engineering experience</li><li>2+ years in a lead or staff role</li><li>Strong full-stack skills</li><li>Passion for sustainability and impact</li></ul>")
                .benefits("<ul><li>Leadership role with high impact</li><li>Significant equity package</li><li>Full benefits</li><li>Carbon-neutral office</li><li>Annual impact bonus</li></ul>")
                .deadline(distant).build());

        jobRepository.save(Job.builder()
                .company(greenbridge).title("Sustainability Data Analyst").slug("sustainability-data-analyst-greenbridge")
                .type(JobType.FULL_TIME).workplaceType(WorkplaceType.ONSITE).experienceLevel(ExperienceLevel.JUNIOR)
                .status(JobStatus.PUBLISHED).isActive(true)
                .salary(Salary.builder().min(60000.0).max(80000.0).currency("USD").period(Salary.SalaryPeriod.YEARLY).isNegotiable(false).build())
                .location(Location.builder().city("Austin").state("TX").country("USA").zipCode("78701").build())
                .skills(Arrays.asList("SQL", "Excel", "ESG Reporting", "Python", "Data Visualization"))
                .description("<p>We are looking for a Sustainability Data Analyst to help enterprise clients track their carbon footprint and ESG metrics using our platform.</p>")
                .responsibilities("<ul><li>Analyze client sustainability data</li><li>Build ESG reporting dashboards</li><li>Support clients with data integration</li><li>Collaborate with the product team on new analytics features</li></ul>")
                .requirements("<ul><li>1+ years of data analysis experience</li><li>Understanding of ESG frameworks (GHG Protocol, CDP)</li><li>Strong SQL and Excel skills</li><li>Passion for sustainability</li></ul>")
                .benefits("<ul><li>Mission-driven work</li><li>Sustainability incentives</li><li>Health and dental</li><li>Professional development budget</li></ul>")
                .deadline(later).build());

        jobRepository.save(Job.builder()
                .company(greenbridge).title("Mobile Developer (React Native)").slug("mobile-developer-greenbridge")
                .type(JobType.FULL_TIME).workplaceType(WorkplaceType.REMOTE).experienceLevel(ExperienceLevel.MID)
                .status(JobStatus.PUBLISHED).isActive(true)
                .salary(Salary.builder().min(88000.0).max(115000.0).currency("USD").period(Salary.SalaryPeriod.YEARLY).isNegotiable(true).build())
                .location(Location.builder().city("Remote").state("").country("USA").zipCode("").build())
                .skills(Arrays.asList("React Native", "TypeScript", "iOS", "Android", "REST APIs", "Firebase"))
                .description("<p>GreenBridge is expanding to mobile and needs a React Native developer to build our iOS and Android apps for enterprise sustainability tracking.</p>")
                .responsibilities("<ul><li>Build cross-platform mobile apps with React Native</li><li>Integrate with our REST APIs</li><li>Implement offline-first data sync</li><li>Release and maintain apps on App Store and Play Store</li></ul>")
                .requirements("<ul><li>3+ years of React Native experience</li><li>Published apps on App Store or Play Store</li><li>TypeScript proficiency</li><li>Experience with Firebase</li></ul>")
                .benefits("<ul><li>Remote-first</li><li>Tech stipend</li><li>Health benefits</li><li>Equity</li></ul>")
                .deadline(soon).build());

        // One archived/draft job for variety
        jobRepository.save(Job.builder()
                .company(techcorp).title("Site Reliability Engineer").slug("sre-techcorp-archived")
                .type(JobType.FULL_TIME).workplaceType(WorkplaceType.ONSITE).experienceLevel(ExperienceLevel.SENIOR)
                .status(JobStatus.ARCHIVED).isActive(false)
                .salary(Salary.builder().min(120000.0).max(160000.0).currency("USD").period(Salary.SalaryPeriod.YEARLY).isNegotiable(false).build())
                .location(Location.builder().city("San Francisco").state("CA").country("USA").zipCode("94105").build())
                .skills(Arrays.asList("SRE", "Go", "Prometheus", "Grafana", "On-call"))
                .description("<p>This position has been filled. Check our other openings!</p>")
                .responsibilities("<ul><li>N/A</li></ul>").requirements("<ul><li>N/A</li></ul>").benefits("<ul><li>N/A</li></ul>")
                .deadline(LocalDate.now().minusDays(10)).build());
    }

    private void seedApplications() {
        List<User> users = userRepository.findAll();
        User alice = users.stream().filter(u -> u.getEmail().equals("alice@example.com")).findFirst().orElse(null);
        User bob = users.stream().filter(u -> u.getEmail().equals("bob@example.com")).findFirst().orElse(null);
        User charlie = users.stream().filter(u -> u.getEmail().equals("charlie@example.com")).findFirst().orElse(null);
        User diana = users.stream().filter(u -> u.getEmail().equals("diana@example.com")).findFirst().orElse(null);
        User eve = users.stream().filter(u -> u.getEmail().equals("eve@example.com")).findFirst().orElse(null);

        List<Job> jobs = jobRepository.findAll();

        Job javaJob = findJob(jobs, "senior-java-backend-engineer-techcorp");
        Job reactJob = findJob(jobs, "frontend-react-developer-techcorp");
        Job devopsJob = findJob(jobs, "devops-engineer-techcorp");
        Job fullstackJob = findJob(jobs, "full-stack-engineer-innovatesoft");
        Job designerJob = findJob(jobs, "product-designer-innovatesoft");
        Job mlJob = findJob(jobs, "ml-engineer-datadriven");
        Job dataAnalystJob = findJob(jobs, "data-analyst-datadriven");
        Job cloudJob = findJob(jobs, "cloud-infra-engineer-cloudnine");
        Job leadJob = findJob(jobs, "lead-software-engineer-greenbridge");
        Job mobileJob = findJob(jobs, "mobile-developer-greenbridge");

        // Alice (Java backend) applies to backend and devops roles
        createApplication(alice, javaJob, ApplicationStatus.SHORTLISTED,
                "5 years of Spring Boot experience, worked on high-traffic microservices at my previous job.");
        createApplication(alice, fullstackJob, ApplicationStatus.UNDER_REVIEW,
                "I have full-stack experience and would love to contribute to developer tools.");
        createApplication(alice, leadJob, ApplicationStatus.PENDING,
                "Looking to step into a lead role. Strong Java and system design background.");

        // Bob (React/frontend) applies to frontend roles
        createApplication(bob, reactJob, ApplicationStatus.ACCEPTED,
                "3 years of React + TypeScript. Built and maintained component libraries at scale.");
        createApplication(bob, designerJob, ApplicationStatus.REJECTED,
                "I can contribute to design-engineering collaboration given my frontend background.");
        createApplication(bob, mobileJob, ApplicationStatus.UNDER_REVIEW,
                "Transitioning into React Native. Have personal projects on the App Store.");

        // Charlie (ML/Python) applies to data roles
        createApplication(charlie, mlJob, ApplicationStatus.SHORTLISTED,
                "MS in Machine Learning. Experience with TensorFlow and MLflow pipelines.");
        createApplication(charlie, dataAnalystJob, ApplicationStatus.PENDING,
                "Strong SQL and Python skills. Familiar with Tableau and statistical analysis.");

        // Diana (DevOps) applies to cloud and infrastructure roles
        createApplication(diana, devopsJob, ApplicationStatus.UNDER_REVIEW,
                "3 years managing Kubernetes clusters on AWS. Terraform certified.");
        createApplication(diana, cloudJob, ApplicationStatus.PENDING,
                "AWS Solutions Architect certified with extensive migration experience.");

        // Eve (UI/UX) applies to design roles
        createApplication(eve, designerJob, ApplicationStatus.SHORTLISTED,
                "3 years of product design for SaaS products. Portfolio: figma.com/eve-portfolio");
        createApplication(eve, reactJob, ApplicationStatus.PENDING,
                "Strong HTML/CSS and some React. Looking to transition into frontend development.");
    }

    private Job findJob(List<Job> jobs, String slug) {
        return jobs.stream().filter(j -> slug.equals(j.getSlug())).findFirst().orElse(null);
    }

    private void createApplication(User candidate, Job job, ApplicationStatus status, String coverLetter) {
        if (candidate == null || job == null) return;
        applicationRepository.save(Application.builder()
                .candidate(candidate)
                .job(job)
                .applicantName(candidate.getName())
                .applicantEmail(candidate.getEmail())
                .applicantPhone(candidate.getPhone())
                .status(status)
                .coverLetter(coverLetter)
                .experience("3-5 years of relevant industry experience.")
                .education("Bachelor's degree in Computer Science or related field.")
                .build());
    }

    private void printCredentials() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("   SEED DATA LOADED — CREDENTIALS");
        System.out.println("=".repeat(60));
        System.out.println("\n--- USERS ---");
        System.out.println("Admin      : admin@jobportal.com     / Admin@123");
        System.out.println("Candidate 1: alice@example.com       / Password@123");
        System.out.println("Candidate 2: bob@example.com         / Password@123");
        System.out.println("Candidate 3: charlie@example.com     / Password@123");
        System.out.println("Candidate 4: diana@example.com       / Password@123");
        System.out.println("Candidate 5: eve@example.com         / Password@123");
        System.out.println("\n--- COMPANIES ---");
        System.out.println("TechCorp Solutions : hr@techcorp.com          / Company@123");
        System.out.println("InnovateSoft       : careers@innovatesoft.io  / Company@123");
        System.out.println("DataDriven Inc     : jobs@datadriven.ai       / Company@123");
        System.out.println("CloudNine Systems  : talent@cloudnine.dev     / Company@123  (PENDING verification)");
        System.out.println("GreenBridge Tech   : work@greenbridge.co      / Company@123");
        System.out.println("\n--- SUMMARY ---");
        System.out.println("Jobs seeded    : 16 (15 active, 1 archived)");
        System.out.println("Applications   : 12 (across 5 candidates)");
        System.out.println("=".repeat(60) + "\n");
    }
}
