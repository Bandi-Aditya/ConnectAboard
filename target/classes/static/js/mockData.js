/**
 * ConnectAbroad - Phase 1 Mock Data Store
 * Isolated mock data representing realistic community users, posts, jobs, housing, and messaging threads.
 * Designed to be easily replaced by REST API endpoints in future phases.
 */

const mockUsers = [
    {
        id: 101,
        name: "Arjun Reddy",
        title: "Software Engineer",
        companyOrUniv: "TechNord Solutions",
        college: "Sri Indu Institute of Engineering & Technology",
        gradYear: "2021",
        location: "Toronto, Canada",
        country: "Canada",
        city: "Toronto",
        flag: "🇨🇦",
        status: "ABROAD", // ABROAD or ASPIRING
        statusLabel: "Living in Toronto, Canada",
        statusBadgeClass: "badge-abroad",
        movedYear: "2023",
        bio: "Software engineer currently working in Toronto. Happy to help students and alumni preparing for their move to Canada.",
        connectionsCount: 486,
        avatarInitials: "AR",
        avatarBg: "#2563eb",
        isAlumni: true,
        hometown: "Hyderabad, India"
    },
    {
        id: 102,
        name: "Priya Sharma",
        title: "Master's Student (Data Analytics)",
        companyOrUniv: "Monash University",
        college: "Sri Indu Institute of Engineering & Technology",
        gradYear: "2022",
        location: "Melbourne, Australia",
        country: "Australia",
        city: "Melbourne",
        flag: "🇦🇺",
        status: "ABROAD",
        statusLabel: "Living in Melbourne, Australia",
        statusBadgeClass: "badge-abroad",
        movedYear: "2024",
        bio: "Sharing student life, accommodation tips, and part-time job opportunities for newcomers in Melbourne.",
        connectionsCount: 312,
        avatarInitials: "PS",
        avatarBg: "#7c3aed",
        isAlumni: true,
        hometown: "Vijayawada, India"
    },
    {
        id: 103,
        name: "Rahul Varma",
        title: "Data Analyst",
        companyOrUniv: "Capital Insights Inc",
        college: "JNTU Hyderabad",
        gradYear: "2020",
        location: "Dallas, USA",
        country: "USA",
        city: "Dallas",
        flag: "🇺🇸",
        status: "ABROAD",
        statusLabel: "Living in Dallas, USA",
        statusBadgeClass: "badge-abroad",
        movedYear: "2022",
        bio: "Always happy to help fellow graduates preparing for the US job market and OPT transitions.",
        connectionsCount: 620,
        avatarInitials: "RV",
        avatarBg: "#059669",
        isAlumni: false,
        hometown: "Hyderabad, India"
    },
    {
        id: 104,
        name: "Sneha Rao",
        title: "Frontend Developer",
        companyOrUniv: "FinTech Digital UK",
        college: "Osmania University",
        gradYear: "2019",
        location: "London, UK",
        country: "UK",
        city: "London",
        flag: "🇬🇧",
        status: "ABROAD",
        statusLabel: "Living in London, UK",
        statusBadgeClass: "badge-abroad",
        movedYear: "2021",
        bio: "Building tech products in London. Passionate about connecting with Indian tech professionals and students in the UK.",
        connectionsCount: 540,
        avatarInitials: "SR",
        avatarBg: "#d97706",
        isAlumni: false,
        hometown: "Hyderabad, India"
    },
    {
        id: 105,
        name: "Kiran Kumar",
        title: "Graduate Student (CS)",
        companyOrUniv: "University of British Columbia",
        college: "Chaitanya Bharathi Institute of Technology",
        gradYear: "2023",
        location: "Vancouver, Canada",
        country: "Canada",
        city: "Vancouver",
        flag: "🇨🇦",
        status: "ABROAD",
        statusLabel: "Living in Vancouver, Canada",
        statusBadgeClass: "badge-abroad",
        movedYear: "2024",
        bio: "Planning to help newcomers find their feet in Vancouver. Organizing local weekend community meetups.",
        connectionsCount: 290,
        avatarInitials: "KK",
        avatarBg: "#dc2626",
        isAlumni: false,
        hometown: "Warangal, India"
    },
    {
        id: 106,
        name: "Vivek Reddy",
        title: "Final Year B.Tech Student",
        companyOrUniv: "Sri Indu Institute of Engineering & Technology",
        college: "Sri Indu Institute of Engineering & Technology",
        gradYear: "2025",
        location: "Hyderabad, India",
        country: "India",
        city: "Hyderabad",
        flag: "🇮🇳",
        status: "ASPIRING",
        statusLabel: "Moving to Toronto, Canada (Fall 2025)",
        statusBadgeClass: "badge-aspiring",
        targetCountry: "Canada",
        targetCity: "Toronto",
        targetFlag: "🇨🇦",
        bio: "Aspiring Master's student preparing for Toronto universities. Eager to connect with alumni already in Canada.",
        connectionsCount: 145,
        avatarInitials: "VR",
        avatarBg: "#0284c7",
        isAlumni: true,
        hometown: "Hyderabad, India"
    },
    {
        id: 107,
        name: "Ananya Rao",
        title: "Software Developer",
        companyOrUniv: "Infosys India",
        college: "VNR Vignana Jyothi Institute",
        gradYear: "2022",
        location: "Hyderabad, India",
        country: "India",
        city: "Hyderabad",
        flag: "🇮🇳",
        status: "ASPIRING",
        statusLabel: "Moving to Berlin, Germany",
        statusBadgeClass: "badge-aspiring",
        targetCountry: "Germany",
        targetCity: "Berlin",
        targetFlag: "🇩🇪",
        bio: "Preparing for job opportunities and language certifications for moving to Germany in 2025.",
        connectionsCount: 210,
        avatarInitials: "AR",
        avatarBg: "#e11d48",
        isAlumni: false,
        hometown: "Hyderabad, India"
    },
    {
        id: 108,
        name: "Rohan Mehta",
        title: "Supply Chain Analyst",
        companyOrUniv: "Logistics Hub Canada",
        college: "Sri Indu Institute of Engineering & Technology",
        gradYear: "2020",
        location: "Montreal, Canada",
        country: "Canada",
        city: "Montreal",
        flag: "🇨🇦",
        status: "ABROAD",
        statusLabel: "Living in Montreal, Canada",
        statusBadgeClass: "badge-abroad",
        movedYear: "2022",
        bio: "Working in supply chain management. Reach out if you're moving to Quebec or eastern Canada.",
        connectionsCount: 380,
        avatarInitials: "RM",
        avatarBg: "#4f46e5",
        isAlumni: true,
        hometown: "Nizamabad, India"
    }
];

const mockPosts = [
    {
        id: 201,
        authorId: 101,
        authorName: "Arjun Reddy",
        authorTitle: "Software Engineer · Toronto 🇨🇦",
        authorAvatar: "AR",
        avatarBg: "#2563eb",
        badgeText: "Living in Toronto",
        badgeClass: "badge-abroad",
        location: "Toronto, Canada",
        timeAgo: "2 hours ago",
        category: "job",
        content: "One of the Indian restaurants near Scarborough is looking for someone for evening shifts (5 PM - 10 PM). If anyone from our community is looking for a legitimate part-time opportunity, message me directly and I'll share the manager's contact details.",
        likesCount: 18,
        commentsCount: 6,
        isLiked: false,
        tag: "Part-Time Job Lead"
    },
    {
        id: 202,
        authorId: 102,
        authorName: "Priya Sharma",
        authorTitle: "Master's Student · Melbourne 🇦🇺",
        authorAvatar: "PS",
        avatarBg: "#7c3aed",
        badgeText: "Living in Melbourne",
        badgeClass: "badge-abroad",
        location: "Melbourne, Australia",
        timeAgo: "5 hours ago",
        category: "housing",
        content: "One private furnished room is becoming available near Clayton from September 1st. Ideally looking for a student. It's roughly a 10-minute walk from the station and super close to Monash campus. DM if anyone is searching for accommodation!",
        likesCount: 31,
        commentsCount: 9,
        isLiked: true,
        tag: "Housing Availability"
    },
    {
        id: 203,
        authorId: 103,
        authorName: "Rahul Varma",
        authorTitle: "Data Analyst · Dallas 🇺🇸",
        authorAvatar: "RV",
        avatarBg: "#059669",
        badgeText: "Living in Dallas",
        badgeClass: "badge-abroad",
        location: "Dallas, USA",
        timeAgo: "1 day ago",
        category: "experience",
        content: "One crucial lesson I wish someone had stressed before I moved to the US: don't underestimate your initial 2-3 months of setup expenses (security deposits, winter clothing, initial groceries). Always maintain a dedicated emergency fund before landing!",
        likesCount: 74,
        commentsCount: 15,
        isLiked: false,
        tag: "Advice & Experience"
    },
    {
        id: 204,
        authorId: 104,
        authorName: "Sneha Rao",
        authorTitle: "Frontend Developer · London 🇬🇧",
        authorAvatar: "SR",
        avatarBg: "#d97706",
        badgeText: "Living in London",
        badgeClass: "badge-abroad",
        location: "London, UK",
        timeAgo: "2 days ago",
        category: "alumni",
        content: "Just reconnected with 3 fellow alumni from our college working here in London tech sector! If anyone else from Sri Indu or Osmania is currently around London, let's form a casual WhatsApp group and meet up.",
        likesCount: 42,
        commentsCount: 11,
        isLiked: false,
        tag: "Alumni Networking"
    },
    {
        id: 205,
        authorId: 106,
        authorName: "Vivek Reddy",
        authorTitle: "Aspiring Master's Student · Hyderabad 🇮🇳",
        authorAvatar: "VR",
        avatarBg: "#0284c7",
        badgeText: "Moving to Toronto",
        badgeClass: "badge-aspiring",
        location: "Hyderabad → Toronto",
        timeAgo: "3 hours ago",
        category: "question",
        content: "Anyone from Sri Indu Institute currently living in Toronto? I'm finalizing my visa process for Fall intake and would love a 10-minute chat regarding neighborhood choices and winter prep advice.",
        likesCount: 12,
        commentsCount: 8,
        isLiked: false,
        tag: "Community Question"
    },
    {
        id: 206,
        authorId: 105,
        authorName: "Kiran Kumar",
        authorTitle: "Graduate Student · Vancouver 🇨🇦",
        authorAvatar: "KK",
        avatarBg: "#dc2626",
        badgeText: "Living in Vancouver",
        badgeClass: "badge-abroad",
        location: "Vancouver, Canada",
        timeAgo: "1 day ago",
        category: "event",
        content: "Planning a small informal coffee meetup for Telugu students and newcomers in Vancouver next Saturday at Stanley Park. Comment below if you'd like to join!",
        likesCount: 28,
        commentsCount: 12,
        isLiked: true,
        tag: "Community Meetup"
    }
];

const mockJobs = [
    {
        id: 301,
        title: "Evening Kitchen Assistant",
        location: "Toronto, Canada 🇨🇦",
        type: "Part-Time",
        pay: "$18.50 - $21.00 / hr",
        postedBy: "Arjun Reddy",
        postedTime: "2 hours ago",
        description: "Reputable dining establishment near Scarborough looking for evening shift assistance (5 PM - 10 PM, 3-4 days/week). Perfect for university students."
    },
    {
        id: 302,
        title: "Restaurant & Counter Staff",
        location: "Melbourne, Australia 🇦🇺",
        type: "Part-Time",
        pay: "$24.00 - $27.00 / hr",
        postedBy: "Priya Sharma",
        postedTime: "1 day ago",
        description: "Local eatery near Clayton station seeking weekend counter staff. Flexible student hours with immediate start."
    },
    {
        id: 303,
        title: "Junior Frontend Developer Intern",
        location: "London, UK 🇬🇧",
        type: "Internship (Hybrid)",
        pay: "£1,800 / month",
        postedBy: "Sneha Rao",
        postedTime: "3 days ago",
        description: "Fintech startup in Central London accepting applications for 6-month developer internship. React/TypeScript knowledge required."
    },
    {
        id: 304,
        title: "Campus Library Student Assistant",
        location: "Vancouver, Canada 🇨🇦",
        type: "Part-Time (On-Campus)",
        pay: "$17.25 / hr",
        postedBy: "Kiran Kumar",
        postedTime: "4 days ago",
        description: "Student position managing circulation desk and cataloging. Open exclusively to enrolled university students."
    }
];

const mockHousing = [
    {
        id: 401,
        title: "Private Furnished Room near Scarborough",
        location: "Toronto, Canada 🇨🇦",
        rent: "$850 / month",
        availability: "Available Sept 1st",
        details: "Utilities included, high-speed Wi-Fi, 5-min walk to bus stop. Looking for clean student or working professional.",
        postedBy: "Arjun Reddy"
    },
    {
        id: 402,
        title: "Single Room in 3BHK Apartment",
        location: "Melbourne, Australia 🇦🇺",
        rent: "$700 / month",
        availability: "Immediate Start",
        details: "10-minute walk to Monash University Clayton campus. Shared kitchen and living space with friendly student roommates.",
        postedBy: "Priya Sharma"
    },
    {
        id: 403,
        title: "Shared Room for Male Student",
        location: "Dallas, Texas, USA 🇺🇸",
        rent: "$450 / month",
        availability: "Available Next Month",
        details: "Located near UT Dallas transit shuttle. In-unit washer/dryer and swimming pool access in quiet apartment community.",
        postedBy: "Rahul Varma"
    }
];

const mockCommunities = [
    {
        id: 501,
        name: "Indians in Toronto 🇨🇦",
        members: "2,450 members",
        category: "City Community",
        description: "The primary hub for Indians living, studying, and working across the Greater Toronto Area."
    },
    {
        id: 502,
        name: "Telugu Students in Melbourne 🇦🇺",
        members: "1,120 members",
        category: "Student Network",
        description: "Connecting Telugu students at Monash, RMIT, and Melbourne Uni for housing, guidance, and events."
    },
    {
        id: 503,
        name: "Sri Indu Alumni Abroad 🎓",
        members: "486 members",
        category: "College Alumni",
        description: "Official global network for Sri Indu Institute graduates working or studying overseas."
    },
    {
        id: 504,
        name: "Indian Developers in London 🇬🇧",
        members: "3,200 members",
        category: "Professional Network",
        description: "Community of Indian tech professionals, software engineers, and founders in London."
    }
];

const mockMessages = [
    {
        id: 601,
        userId: 101,
        userName: "Arjun Reddy",
        userAvatar: "AR",
        avatarBg: "#2563eb",
        lastMsg: "Sure! I can share the restaurant manager's email directly with you.",
        time: "10:42 AM",
        unread: false,
        conversation: [
            { sender: "Arjun", text: "Hey Aditya! Saw your profile — planning to move to Toronto soon?", time: "10:30 AM" },
            { sender: "You", text: "Yes Arjun! Trying to understand the job situation and housing costs near Scarborough.", time: "10:35 AM" },
            { sender: "Arjun", text: "Awesome. I've been here since 2023. Scarborough is quite student-friendly for housing.", time: "10:38 AM" },
            { sender: "Arjun", text: "Sure! I can share the restaurant manager's email directly with you.", time: "10:42 AM" }
        ]
    },
    {
        id: 602,
        userId: 102,
        userName: "Priya Sharma",
        userAvatar: "PS",
        avatarBg: "#7c3aed",
        lastMsg: "The Clayton room is still available if you're interested!",
        time: "Yesterday",
        unread: true,
        conversation: [
            { sender: "Priya", text: "Hi! Are you still looking for accommodation options in Melbourne?", time: "Yesterday" },
            { sender: "Priya", text: "The Clayton room is still available if you're interested!", time: "Yesterday" }
        ]
    },
    {
        id: 603,
        userId: 103,
        userName: "Rahul Varma",
        userAvatar: "RV",
        avatarBg: "#059669",
        lastMsg: "Let me know when you get your visa stamp done.",
        time: "Aug 20",
        unread: false,
        conversation: [
            { sender: "Rahul", text: "Hey! Happy to connect with fellow graduates.", time: "Aug 20" },
            { sender: "Rahul", text: "Let me know when you get your visa stamp done.", time: "Aug 20" }
        ]
    }
];
