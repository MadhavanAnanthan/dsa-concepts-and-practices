# DDIA Chapter 14: Doing the Right Thing - Compressed Study Summary

Book: DDIA  
Chapter: 14 - Doing the Right Thing  
Source PDF: books/DDIA/DDIA.pdf  
Source PDF/pages: PDF pages 609-622; printed pages 585-598  
Method: Compressed from `books/DDIA/raw/chapter-14-chapter-14-doing-the-right-thing.md` only. Long passages are paraphrased; no external knowledge used.

## Source Map

| Section | Source pages |
| --- | --- |
| Chapter framing: engineering responsibility and ethics | PDF 609-610; pp. 585-586 |
| Predictive Analytics | PDF 610; p. 586 |
| Bias and Discrimination | PDF 610-611; pp. 586-587 |
| Responsibility and Accountability | PDF 611-612; pp. 587-588 |
| Feedback Loops | PDF 612-613; pp. 588-589 |
| Privacy and Tracking | PDF 613-614; pp. 589-590 |
| Surveillance | PDF 614-615; pp. 590-591 |
| Consent and Freedom of Choice | PDF 615-616; pp. 591-592 |
| Privacy and Use of Data | PDF 616-618; pp. 592-594 |
| Data as Assets and Power | PDF 618-619; pp. 594-595 |
| Remembering the Industrial Revolution | PDF 619-620; pp. 595-596 |
| Legislation and Self-Regulation | PDF 620-621; pp. 596-597 |
| Summary | PDF 621-622; pp. 597-598 |

## Section-Level Summary

### 1. Engineering Responsibility and Ethics

Source: PDF 609-610; pp. 585-586

Chapter 14 closes DDIA by moving from architecture to responsibility. Data systems are built for purposes, and their consequences can reach far beyond technical correctness or profit. Many datasets describe people: their behavior, identity, interests, relationships, health, movement, and economic life. Engineers therefore have a responsibility to consider whether their systems cause harm.

The chapter rejects the idea that ethics is a simple compliance checklist. Ethical reasoning is described as an iterative process involving reflection, dialogue with affected people, and accountability for outcomes. The core memory hook is: technology is not judged only by what it can do, but by how it is used and whom it affects.

### 2. Predictive Analytics

Source: PDF 610; p. 586

Predictive analytics becomes ethically difficult when it affects individual lives. Predicting weather or disease spread is different from predicting whether a person may reoffend, default on a loan, make insurance claims, or perform well in a job. Organizations often have incentives to reject uncertain cases because accepting a risky loan, employee, passenger, or transaction can be costly.

The harm appears when many automated systems repeatedly say no to someone labeled risky. The chapter uses the idea of "algorithmic prison": automated exclusion from jobs, housing, travel, insurance, finance, and other basic opportunities, often with little proof and little appeal. The interview point is to ask who bears the cost of caution.

### 3. Bias and Discrimination

Source: PDF 610-611; pp. 586-587

Algorithmic decisions are not automatically fairer than human decisions. Data-driven systems may help counter subjective bias, but ML systems infer rules from data rather than having all rules explicitly programmed. If the input data reflects historical discrimination, the model can learn and amplify it.

Protected traits such as ethnicity, age, gender, sexuality, disability, and beliefs may be legally sensitive, but proxy variables can reintroduce the same discrimination. Postal code and IP address can correlate with race in segregated areas. The chapter's key warning is that predictive systems extrapolate from the past; if the past is discriminatory, models may encode that discrimination unless humans deliberately choose a better objective.

### 4. Responsibility and Accountability

Source: PDF 611-612; pp. 587-588

Automated decisions make accountability harder. If a human makes a harmful decision, the affected person may appeal and the decision-maker may be responsible. If an algorithm makes the decision, organizations must not evade responsibility by blaming the model.

The chapter contrasts traditional credit scores with broader ML scoring. A credit score is usually tied to a person's own borrowing history and errors can in principle be corrected. Predictive analytics often asks how similar people behaved, which introduces stereotyping and group-based inference. Because statistical predictions can be correct on average but wrong for a person, affected individuals need transparency, recourse, and correction mechanisms.

### 5. Feedback Loops

Source: PDF 612-613; pp. 588-589

Predictive systems can create feedback loops. Recommendation systems may show users only views they already agree with, contributing to echo chambers, misinformation, stereotypes, and polarization. More consequential systems can trap people in worsening conditions: the chapter's example is credit-score-based hiring, where financial hardship lowers credit score, lowers employability, worsens finances, and repeats.

Another example is algorithmic gas pricing in Germany, where pricing algorithms reduced competition and increased consumer prices by learning collusive behavior. The chapter recommends systems thinking: analyze the whole socio-technical system, including people and incentives, and ask whether the system amplifies or counteracts inequality.

### 6. Privacy and Tracking

Source: PDF 613-614; pp. 589-590

Privacy concerns are not limited to automated decisions; data collection itself changes the relationship between organizations and people. If users explicitly enter data for a service, the system is acting for the user. If behavior is tracked as a side effect, the service may serve its own interests or advertisers' interests.

Some tracking helps users, such as search click tracking, related-product recommendations, and A/B testing. But advertising-funded business models can turn users into the product-like source of behavioral profiles. When advertisers are the customers, tracking can become detailed, long-retained, and aimed at marketing rather than user benefit. The chapter names this relationship surveillance.

### 7. Surveillance

Source: PDF 614-615; pp. 590-591

The chapter uses the word surveillance to make the power relationship visible. Digitization has made large-scale personal data collection cheap and pervasive. Connected devices such as smartphones, smart TVs, voice assistants, baby monitors, and toys can create always-present sensors, often with poor security.

Modern systems can track movement, communication, purchases, payments, relationships, and health. They may infer illnesses or economic trouble before the person recognizes them. The "nothing to hide" argument is weak because it assumes safety within current power structures; marginalized people may face real danger. Surveillance also becomes more serious when behavioral data affects insurance, employment, or other important decisions.

### 8. Consent and Freedom of Choice

Source: PDF 615-616; pp. 591-592

The chapter challenges the claim that users freely consent to tracking. First, not all tracking is clearly necessary for user benefit. Second, users usually do not understand what data is collected, retained, combined, and processed, especially when derived datasets mix behavior with other users' data and external sources. Third, the relationship is asymmetric: services set the terms, and users cannot negotiate the data-for-service exchange.

The GDPR standard cited in the chapter requires consent to be freely given, specific, informed, unambiguous, and withdrawable without detriment. But even outside legal details, a service can be effectively mandatory if it is essential for social participation or has strong network effects. Opting out may require time, knowledge, privilege, and willingness to lose social or professional opportunities.

### 9. Privacy and Use of Data

Source: PDF 616-618; pp. 592-594

Privacy does not mean keeping everything secret. It means having the freedom to choose what to reveal, to whom, and for what purpose. A person might share medical data with researchers but not want insurers or employers to use it. Privacy is therefore a decision right and a form of autonomy.

Surveillance transfers practical control from individuals to data collectors. Companies may keep sensitive inferences hidden while using them indirectly, such as through ad targeting. Even if an ad group does not personally identify a user, the user has lost agency over disclosure. Privacy settings that control visibility to other users are only partial because the service itself may still analyze the data internally. Systems must expect wrong, painful, undesirable, or inappropriate data use and provide mechanisms to handle those failures.

### 10. Data as Assets and Power

Source: PDF 618-619; pp. 594-595

The chapter criticizes the phrase "data exhaust" because it makes behavioral data sound like worthless waste. If targeted advertising funds the service, user activity is economically valuable and can even be understood as labor. Data brokers, user-number valuations, and advertising markets show that personal data is a valuable asset.

But personal data is also hazardous. Companies want it, governments may seek it, bankrupt firms may sell it, insiders may leak it, criminals may steal it, and future management or political regimes may misuse it. The chapter frames data as a toxic asset or hazardous material: valuable, but dangerous. The power issue is information asymmetry: organizations scrutinize people while avoiding equivalent scrutiny themselves.

### 11. Remembering the Industrial Revolution

Source: PDF 619-620; pp. 595-596

The chapter compares the information age to the Industrial Revolution. Industrialization produced long-term economic growth and better living standards, but it also produced pollution, unsafe labor, child labor, poor housing, and other harms before safeguards were established. Environmental rules, workplace safety, child labor laws, and food inspections increased business costs, but society benefited.

The analogy is that data collection and misuse are the information age's major externalized harms. Data is compared to pollution: it accumulates, persists, and must be contained or disposed of responsibly. The memory hook is that innovation without safeguards can create costs that society later has to clean up.

### 12. Legislation and Self-Regulation

Source: PDF 620-621; pp. 596-597

Data protection laws can help preserve individual rights. The chapter cites GDPR principles of specified, explicit, legitimate purposes and data minimization. These principles conflict with the big-data instinct to collect as much as possible, combine datasets, and explore unforeseen uses.

The chapter acknowledges the hard balance: regulation may limit harmful surveillance, but overregulation can also block beneficial uses such as medical research. Its main recommendation is a culture shift. Technology workers should stop treating users as metrics, respect dignity and agency, educate users, self-regulate data practices, purge data when no longer needed, and minimize collection. Data not collected cannot be leaked, stolen, sold, or compelled.

### 13. Book Closure

Source: PDF 621-622; pp. 597-598

The chapter ends by recapping the whole book: operational and analytical systems, nonfunctional requirements, data models, storage engines, encoding, replication, sharding, transactions, distributed systems faults, consensus, batch processing, stream processing, and streaming-system philosophy.

The final message is that data can do good but can also cause serious harm through hard-to-appeal decisions, discrimination, exploitation, surveillance, exposure of intimate information, data breaches, and unintended consequences. Engineers must work toward systems and societies that treat people with humanity and respect.

## Chapter-Level Memory Hooks

- Data systems often act on people, not just records.
- Ethics is dialogue, accountability, and iteration.
- Predictive analytics can become automated exclusion.
- Historical bias plus model training can amplify discrimination.
- Proxy variables can recreate protected-trait discrimination.
- Statistical accuracy does not guarantee individual fairness.
- Feedback loops make model outputs shape future reality.
- Privacy is contextual control, not secrecy.
- Consent requires understanding and real ability to refuse.
- Surveillance is about power and incentives.
- Personal data is both valuable and hazardous.
- Data minimization reduces future misuse, breach, and coercion risk.

## Interview Perspective

- For ethical ML: discuss bias, proxy variables, explainability, recourse, and feedback loops.
- For privacy: discuss purpose limitation, minimization, deletion, internal access, inference risk, and meaningful consent.
- For product telemetry: separate user-benefiting data collection from advertising-driven surveillance.
- For architecture reviews: ask what is collected, why, for how long, who can access it, what can be inferred, and what happens under breach, acquisition, bankruptcy, or government compulsion.

## Final Takeaways

- Chapter 14 argues that technical excellence is incomplete without responsibility for human consequences.
- Predictive analytics must be designed with accountability, transparency, appeal, and fairness in mind.
- Privacy is a person's agency over data disclosure and use.
- Consent is weak when users cannot understand or realistically refuse tracking.
- Data should be treated like a hazardous valuable asset: collect less, keep it for less time, and design for misuse risk.
- Engineers are responsible for considering societal impact as part of doing the job well.

Confidence: High
