package com.connectabroad.config;

import com.connectabroad.entity.*;
import com.connectabroad.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final CollegeRepository collegeRepository;
    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final ReportRepository reportRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ProfileRepository profileRepository,
                           CollegeRepository collegeRepository,
                           CommunityRepository communityRepository,
                           CommunityMemberRepository communityMemberRepository,
                           ReportRepository reportRepository,
                           AdminAuditLogRepository auditLogRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.collegeRepository = collegeRepository;
        this.communityRepository = communityRepository;
        this.communityMemberRepository = communityMemberRepository;
        this.reportRepository = reportRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        College siiett = collegeRepository.findByNameIgnoreCase("Sri Indu Institute of Engineering & Technology")
                .orElseGet(() -> collegeRepository.save(new College(
                        "Sri Indu Institute of Engineering & Technology",
                        "Hyderabad",
                        "Telangana",
                        "India"
                )));

        College monash = collegeRepository.findByNameIgnoreCase("Monash University")
                .orElseGet(() -> collegeRepository.save(new College(
                        "Monash University",
                        "Melbourne",
                        "Victoria",
                        "Australia"
                )));

        User admin = null;
        if (!userRepository.existsByEmail("admin@example.com")) {
            admin = new User(
                    "Platform Administrator",
                    "admin@example.com",
                    passwordEncoder.encode("admin123"),
                    Role.ADMIN,
                    UserType.ABROAD
            );
            userRepository.save(admin);

            Profile adminProfile = new Profile(admin);
            adminProfile.setCollege(siiett);
            adminProfile.setDegree("M.S. Computer Science");
            adminProfile.setGraduationYear(2020);
            adminProfile.setHometown("Hyderabad, India");
            adminProfile.setCurrentCountry("United States");
            adminProfile.setCurrentCity("San Francisco");
            adminProfile.setProfession("Platform Admin");
            adminProfile.setExperienceYears(5);
            adminProfile.setBio("System Administrator for ConnectAbroad platform.");
            profileRepository.save(adminProfile);
        } else {
            admin = userRepository.findByEmail("admin@example.com").orElse(null);
        }

        User arjun = null;
        if (!userRepository.existsByEmail("arjun@example.com")) {
            arjun = new User(
                    "Arjun Reddy",
                    "arjun@example.com",
                    passwordEncoder.encode("password123"),
                    Role.USER,
                    UserType.ABROAD
            );
            userRepository.save(arjun);

            Profile arjunProfile = new Profile(arjun);
            arjunProfile.setCollege(siiett);
            arjunProfile.setDegree("B.Tech Computer Science");
            arjunProfile.setGraduationYear(2021);
            arjunProfile.setHometown("Hyderabad, India");
            arjunProfile.setCurrentCountry("Canada");
            arjunProfile.setCurrentCity("Toronto");
            arjunProfile.setMovedYear(2023);
            arjunProfile.setProfession("Software Engineer");
            arjunProfile.setExperienceYears(3);
            arjunProfile.setSkills("Java, Spring Boot, React, Microservices");
            arjunProfile.setBio("Software engineer currently working in Toronto. Happy to help students and alumni preparing for their move to Canada.");
            profileRepository.save(arjunProfile);
        } else {
            arjun = userRepository.findByEmail("arjun@example.com").orElse(null);
        }

        User vivek = null;
        if (!userRepository.existsByEmail("vivek@example.com")) {
            vivek = new User(
                    "Vivek Reddy",
                    "vivek@example.com",
                    passwordEncoder.encode("password123"),
                    Role.USER,
                    UserType.ASPIRING
            );
            userRepository.save(vivek);

            Profile vivekProfile = new Profile(vivek);
            vivekProfile.setCollege(siiett);
            vivekProfile.setDegree("B.Tech Computer Science");
            vivekProfile.setGraduationYear(2025);
            vivekProfile.setHometown("Hyderabad, India");
            vivekProfile.setCurrentCountry("India");
            vivekProfile.setCurrentCity("Hyderabad");
            vivekProfile.setTargetCountry("Canada");
            vivekProfile.setTargetCity("Toronto");
            vivekProfile.setTargetUniversity("University of Toronto");
            vivekProfile.setExpectedMoveDate(LocalDate.of(2025, 9, 1));
            vivekProfile.setProfession("Final Year Student / Trainee");
            vivekProfile.setExperienceYears(0);
            vivekProfile.setSkills("Java, Spring Boot, SQL, Git");
            vivekProfile.setBio("Aspiring Master's student preparing for Toronto universities. Eager to connect with alumni already in Canada.");
            profileRepository.save(vivekProfile);
        } else {
            vivek = userRepository.findByEmail("vivek@example.com").orElse(null);
        }

        if (!userRepository.existsByEmail("priya@example.com")) {
            User priya = new User(
                    "Priya Sharma",
                    "priya@example.com",
                    passwordEncoder.encode("password123"),
                    Role.USER,
                    UserType.ABROAD
            );
            userRepository.save(priya);

            Profile priyaProfile = new Profile(priya);
            priyaProfile.setCollege(monash);
            priyaProfile.setDegree("M.S. Data Science");
            priyaProfile.setGraduationYear(2022);
            priyaProfile.setHometown("Mumbai, India");
            priyaProfile.setCurrentCountry("Australia");
            priyaProfile.setCurrentCity("Melbourne");
            priyaProfile.setMovedYear(2021);
            priyaProfile.setProfession("Data Analyst");
            priyaProfile.setExperienceYears(4);
            priyaProfile.setSkills("Python, SQL, Tableau, Machine Learning");
            priyaProfile.setBio("Data Analyst living in Melbourne. Passionate about helping international students adapt to life in Australia.");
            profileRepository.save(priyaProfile);
        }

        if (!userRepository.existsByEmail("aditya@example.com")) {
            User aditya = new User(
                    "Aditya Bandi",
                    "aditya@example.com",
                    passwordEncoder.encode("password123"),
                    Role.USER,
                    UserType.ASPIRING
            );
            userRepository.save(aditya);

            Profile adityaProfile = new Profile(aditya);
            adityaProfile.setCollege(siiett);
            adityaProfile.setDegree("B.Tech Information Technology");
            adityaProfile.setGraduationYear(2024);
            adityaProfile.setHometown("Hyderabad, India");
            adityaProfile.setCurrentCountry("India");
            adityaProfile.setCurrentCity("Hyderabad");
            adityaProfile.setTargetCountry("Canada");
            adityaProfile.setTargetCity("Toronto");
            adityaProfile.setTargetUniversity("York University");
            adityaProfile.setExpectedMoveDate(LocalDate.of(2025, 8, 15));
            adityaProfile.setProfession("Software Engineer Trainee");
            adityaProfile.setExperienceYears(1);
            adityaProfile.setSkills("JavaScript, React, Node.js, Java");
            adityaProfile.setBio("Planning to move to Toronto for graduate studies. Looking to connect with seniors and professionals in Canada.");
            profileRepository.save(adityaProfile);
        }

        // Seed Sample Communities
        if (communityRepository.count() == 0 && arjun != null) {
            Community torontoInds = new Community(
                    "Indians in Toronto & GTA",
                    "Community for Indian students and working professionals living in Toronto and the GTA area.",
                    "Regional Support",
                    "Toronto, Canada",
                    arjun
            );
            communityRepository.save(torontoInds);
            communityMemberRepository.save(new CommunityMember(torontoInds, arjun));

            Community techAbroad = new Community(
                    "Tech & Software Professionals Abroad",
                    "Networking hub for software engineers, product managers, and data scientists moving abroad.",
                    "Career & Networking",
                    "Global",
                    arjun
            );
            communityRepository.save(techAbroad);
            communityMemberRepository.save(new CommunityMember(techAbroad, arjun));
        }

        // Seed Sample Reports
        if (reportRepository.count() == 0 && vivek != null) {
            Report sampleReport = new Report(
                    vivek,
                    ReportTargetType.POST,
                    1L,
                    ReportReason.SPAM,
                    "This post appears to contain promotional spam links."
            );
            reportRepository.save(sampleReport);
        }

        // Seed Initial Audit Log
        if (auditLogRepository.count() == 0 && admin != null) {
            AdminAuditLog initialLog = new AdminAuditLog(
                    admin,
                    "SYSTEM_INIT",
                    "SYSTEM",
                    0L,
                    "Initial Phase 17 Admin Panel & Analytics system initialized."
            );
            auditLogRepository.save(initialLog);
        }
    }
}
