package catalystpage.com.model

import catalystpage.com.util.Constants.FIRST
import catalystpage.com.util.Constants.SECOND
import catalystpage.com.util.Constants.THIRD

enum class Article(
    val year: String,
    val title: String,
    val summary: String,
    val author: String,
    val datePublished: String,
    val path: String

) {
    First(
        year = "2024",
        title = "Modulating the Human Gut Microbiome and Health Markers through Kombucha Consumption: A Controlled Clinical Study",
        summary = "In a small pilot study involving 12 participants with diabetes, researchers examined whether kombucha could help manage blood sugar levels. The study reported a modest reduction in fasting blood glucose levels among those who consumed kombucha daily compared to the control group. While the sample size was limited, the results suggest that kombucha shows promise for further research as a potential complementary aid in managing diabetes.",
        author = "Gertrude Ecklu-Mensah, Rachel Miller, Maria Gjerstad Maseng, Vienna Hawes, Denise Hinz, Cheryl Kim, Jack Gilbert",
        datePublished = "2024.07.1",
        path = FIRST
    ),
    Second(
        year = "2024",
        title = "Evaluating the Health Implications of Kombucha Fermented with Gardenia jasminoides Teas: A Comprehensive Analysis of Antioxidant, Antimicrobial, and Cytotoxic Properties",
        summary = "This research examined kombucha brewed with Gardenia jasminoides tea, revealing potent antioxidant, antimicrobial, and cytotoxic properties. Statistical evaluation demonstrated significant health-promoting effects, making this unique kombucha variant a promising functional beverage.",
        author = "Gayathree Thenuwara, Xu Cui, Zhen Yao, Bilal Javed, Azza Silotry Naik, and Furong Tian",
        datePublished = "2024.12.15",
        path = SECOND
    ),
    Third(
        year = "2023",
        title = "Kombucha tea as an anti-hyperglycemic agent in humans with diabetes – a randomized controlled pilot investigation",
        summary = "This pilot study explored the potential of kombucha as a complementary intervention for diabetic management. Statistical analysis revealed a significant reduction in fasting blood glucose levels among participants consuming kombucha regularly. Despite the study's limited sample size, the promising results suggest further clinical trials could validate kombucha's anti-hyperglycemic properties.",
        author = "Chagai Mendelson, Sabrina Sparkes, Daniel J Merenstein, Chloe Christensen, Varun Sharma, Sameer Desale, Jennifer M Auchtung, Car Reen Kok, Heather E Hallen Adams, Robert Hutkins",
        datePublished = "2023.08.1",
        path = THIRD
    )





}