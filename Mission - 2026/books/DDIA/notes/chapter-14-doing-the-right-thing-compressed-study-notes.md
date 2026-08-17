# DDIA Chapter 14: Doing the Right Thing - Compressed Study Notes

Book: DDIA  
Chapter: 14 - Doing the Right Thing  
Source PDF: books/DDIA/DDIA.pdf  
Source PDF/pages: PDF pages 609-622; printed pages 585-598  
Method: Compressed section-by-section from `books/DDIA/raw/chapter-14-chapter-14-doing-the-right-thing.md` only. Long passages are paraphrased; important concepts, examples, tradeoffs, terminology, edge cases, limitations, and interview hooks are preserved. No external knowledge used.

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

## 1. Chapter Framing: Ethics Belongs in Data Systems

Source: PDF 609-610; pp. 585-586

- The final chapter steps back from the book's technical architecture focus and asks what responsibilities engineers carry when building data-intensive systems.
- Every system has a purpose and consequences. Some consequences are intended; others are not. Engineers must consider both.
- Data often describes people: behavior, interests, identity, relationships, finances, health, and movement. Treating data abstractly can hide the fact that the system affects human dignity.
- Software systems are not ethically neutral in practice. A technology's impact depends on how it is used and how it affects people.
- Professional codes of ethics exist, but the chapter argues that they are often not meaningfully applied or enforced in day-to-day software work.
- Ethics is not presented as a checklist. It is an ongoing process of reflection, participation, dialogue with affected people, and accountability for outcomes.

Terminology:

- Ethical responsibility: the obligation to consider consequences and avoid harm, not merely to build technically correct systems.
- Participatory ethics: reasoning about impact with the people involved, instead of deciding privately inside the engineering organization.

Tradeoff:

- Technical optimization alone can improve reliability, scale, or revenue while still worsening human outcomes. The chapter asks engineers to widen the design objective.

Interview hook: A senior systems answer should not stop at "can we build it?" For people-related data, also ask "who can be harmed, who has power, and who can appeal?"

## 2. Predictive Analytics

Source: PDF 610; p. 586

- Predictive analytics is one of the main attractions of big data and AI, but it becomes ethically risky when predictions directly affect individuals.
- Low-stakes or aggregate predictions, such as weather or disease spread, differ from predictions about whether a person might reoffend, default on a loan, make expensive insurance claims, or be a good employee.
- Organizations have rational incentives to avoid bad outcomes: fraud, bad loans, hijackings, and unsuitable hires. If the cost of false acceptance seems higher than the cost of false rejection, they may prefer to say no when uncertain.
- At population scale, repeated algorithmic rejections can seriously restrict someone's ability to participate in society.
- The chapter uses the idea of "algorithmic prison" for cases where automated risk labels exclude people from jobs, travel, insurance, rental housing, financial services, and other essential opportunities.
- A core contrast: criminal justice systems that respect human rights assume innocence until guilt is proven; automated systems may exclude people with little proof, little transparency, and little appeal.

Edge cases:

- A risk label can be accurate or false; either way, the person may still suffer repeated denials.
- A decision that looks individually small to one company can become life-shaping when many organizations use similar signals.

Interview hook: Predictive analytics creates a false-negative versus false-positive tradeoff, but the chapter pushes you to ask who bears each cost.

## 3. Bias and Discrimination

Source: PDF 610-611; pp. 586-587

- Algorithmic decisions are not automatically better or worse than human decisions. Humans have biases, and discrimination can become culturally institutionalized.
- There is a legitimate hope that data-driven decisions might be fairer than subjective human judgment, especially for people historically overlooked by traditional systems.
- The key difference in ML-based systems is that engineers do not fully specify the decision rules; the rules are inferred from data.
- Learned patterns may be opaque. A correlation can drive a decision even when the system builders cannot explain why that correlation exists.
- If training data contains systematic bias, the model is likely to learn and amplify that bias.
- Anti-discrimination law often protects traits such as ethnicity, age, gender, sexuality, disability, and beliefs. The difficult case is proxy variables: features not explicitly protected but correlated with protected traits.
- Examples of proxy features include postal code or IP address in racially segregated neighborhoods.
- Predictive systems extrapolate from the past. If the past contains discrimination, the system may reproduce and intensify it.
- The chapter argues that a better future requires human moral imagination; data and models should serve human judgment, not replace it.

Terminology:

- Protected traits: categories that law or ethics may prohibit using as a basis for differential treatment.
- Proxy discrimination: using features that are not protected traits themselves but correlate strongly with protected traits.
- Opaque model behavior: decision patterns that are statistically present but not clearly explainable.

Tradeoff:

- Data can help counter subjective bias, but biased historical data can also make discrimination appear objective.

Limitations:

- Removing explicit protected fields does not necessarily remove discrimination, because correlated features can still carry the same signal.
- Model correlation is not the same as a morally acceptable reason for a decision.

Interview hook: A strong fairness answer distinguishes explicit protected attributes from proxy variables and asks whether the model is reproducing historical injustice.

## 4. Responsibility and Accountability

Source: PDF 611-612; pp. 587-588

- Automated decisions raise the question of who is accountable when the system causes harm.
- Humans can be held responsible and affected people can appeal. Algorithms also make mistakes, but accountability can become unclear if organizations blame the system.
- The chapter gives examples: accidents involving self-driving cars, discriminatory credit scoring, and judicial review of ML decisions.
- Engineers and organizations should not evade responsibility by attributing decisions to an algorithm.
- Credit rating agencies are a long-standing example of data-based decision making. Traditional credit scores are at least normally tied to borrowing history, and errors can in principle be corrected.
- ML scoring systems may use broader inputs and be more opaque, making it harder to know why a decision happened or whether it was unfair.
- A credit score roughly asks how the person behaved in the past. Predictive analytics often asks how similar people behaved in the past. That shift introduces stereotyping.
- Statistical predictions can be valid on average while wrong for individual cases. A population distribution does not determine one person's outcome.
- The chapter warns against blind faith in data. Data-driven decision making needs transparency, accountability, correction mechanisms, and protection against bias.
- Analytics can be used constructively to target aid and support, but the same insight can be used by predatory businesses to identify vulnerable people and sell risky products.

Terminology:

- Recourse: a person's ability to challenge, correct, or appeal a decision.
- Statistical nature of data: aggregate patterns may be useful while individual predictions remain uncertain.

Edge cases:

- Erroneous input data can make an automated decision wrong, and opaque derived features may make correction almost impossible.
- A person can be assigned to the wrong similarity group, and the system may still treat the group-level prediction as personally meaningful.

Interview hook: Ask "Can the affected person understand, challenge, and correct the decision?" If the answer is no, accountability is weak.

## 5. Feedback Loops

Source: PDF 612-613; pp. 588-589

- Even systems with apparently softer effects, such as recommendations, can create difficult societal problems.
- If a service predicts what users want to see and then only shows reinforcing content, it can contribute to echo chambers, stereotypes, misinformation, and polarization.
- Predictive systems can create self-reinforcing feedback loops when their outputs change the conditions they later observe.
- Example: employers using credit scores in hiring. A person suffers financial trouble, misses bills, credit score worsens, hiring prospects decline, unemployment worsens finances, and the cycle repeats.
- Another example: algorithmic gas station pricing in Germany reduced competition and raised consumer prices because pricing algorithms learned collusive behavior.
- Such feedback loops are not always predictable, but many consequences can be anticipated by considering the whole socio-technical system.
- Systems thinking means analyzing both the computerized components and the people who interact with them.
- The design question is whether the system amplifies existing differences, such as making rich people richer and poor people poorer, or whether it tries to counter injustice.

Terminology:

- Feedback loop: a cycle in which system outputs influence future inputs, often reinforcing a pattern.
- Systems thinking: examining the entire system, including people, institutions, incentives, and software.

Tradeoff:

- Optimizing for immediate prediction accuracy or engagement can degrade broader social outcomes.

Interview hook: For ranking, recommendation, pricing, fraud, or scoring systems, always ask how the model's decisions change future data.

## 6. Privacy and Tracking

Source: PDF 613-614; pp. 589-590

- Beyond automated decisions, the act of collecting data itself creates ethical problems.
- The key relationship question is whether the system is serving the user or whether the user's activity is being extracted for another party's benefit.
- If a system stores data that users explicitly enter for a service they want, the user is clearly the customer.
- If the system logs behavior as a side effect of other activity, the relationship becomes less clear. The service develops interests of its own, which may conflict with user interests.
- Some tracking supports user-facing benefits:
  - Click tracking can improve search ranking.
  - Recommendation data can help users discover useful or interesting items.
  - A/B testing and user-flow analysis can improve interfaces.
- The problem escalates when the business model depends on advertising. Advertisers become the actual customers, while users' interests become secondary.
- Advertising-funded services tend to gather detailed behavioral data, retain it for long periods, and build profiles for marketing.
- The chapter describes this relationship as surveillance when tracking primarily serves the data collector or advertiser rather than the user.

Terminology:

- Behavioral tracking: logging user actions, often as a side effect of service usage.
- Surveillance: data collection relationship in which the subject is monitored for another party's interests.

Tradeoff:

- Some data collection genuinely improves features. The ethical issue is whether collection is necessary, proportionate, understandable, and aligned with user interests.

Interview hook: Distinguish service data from surveillance data: who requested the processing, who benefits, and who controls future use?

## 7. Surveillance

Source: PDF 614-615; pp. 590-591

- The chapter suggests replacing the word "data" with "surveillance" in common industry phrases to expose the power relationship behind large-scale tracking.
- Digitization has made mass collection of personal information cheap and scalable.
- Internet-connected microphones, smartphones, smart TVs, voice assistants, baby monitors, and connected toys make it increasingly common for inhabited spaces to contain devices capable of recording or transmitting sensitive data.
- The chapter highlights poor security records for many such devices.
- Modern surveillance can cover location, movement, social relationships, communication, purchases, payments, and health.
- A surveillance organization may infer sensitive facts before the person knows them, such as illness or economic problems.
- Compared with historic authoritarian surveillance, the novelty is that people often voluntarily accept corporate tracking because the digital services are useful.
- Not all data collection is surveillance, but treating it as such can clarify the relationship between data collector and data subject.
- The "nothing to hide" response assumes a person is safe under existing power structures. The chapter warns that marginalized people may not share that safety.
- Surveillance becomes less benign when behavioral data affects insurance, employment, or other important life opportunities.
- Intrusive inference can come from unexpected sensors; the chapter gives the example of smartwatch or fitness tracker motion data being used to infer typing.

Edge cases:

- Data collected for convenience or personalization can later be used for consequential decisions.
- Sensor accuracy and analysis techniques are expected to improve, increasing future inference risk.

Interview hook: Privacy risk is not just "what did the user type?" It is also "what can be inferred later from exhaust, sensors, and correlations?"

## 8. Consent and Freedom of Choice

Source: PDF 615-616; pp. 591-592

- A common defense of tracking is that users voluntarily agree to terms of service and privacy policies and receive valuable services in return.
- The chapter challenges this defense on several grounds.
- First, necessity must be questioned. Click-through tracking for search relevance or purchase co-occurrence for related products may directly support user benefits, but profiling for ads or content recommendation is less clearly in the user's interest.
- Second, most users do not understand what data is collected, retained, combined, and processed. Privacy policies often obscure rather than explain.
- Without understanding, consent is not meaningful.
- Data about one user can reveal things about other people who never agreed to the service's terms.
- Derived datasets combine user behavior with other user data and external sources, making the resulting processing especially hard for users to understand.
- Third, the relationship is one-way and asymmetric. Users generally cannot negotiate what data they provide for what service.
- The chapter references GDPR consent requirements: consent must be freely given, specific, informed, unambiguous, withdrawable without detriment, and requested in clear language; silence or preselected choices do not count.
- Consent is not the only lawful basis for data processing under GDPR; other bases include legal compliance, protecting life, and legitimate interests such as fraud prevention.
- Even when a user can technically decline a service, the choice may not be free if the service is essential for basic social participation.
- Network effects increase the cost of opting out. Smartphones, social networks, and search engines are examples in the chapter.
- Platforms may be deliberately designed to keep users engaged, so opting out requires time, knowledge, privilege, and willingness to lose social or professional opportunities.

Terminology:

- Meaningful consent: consent based on understandable information and real freedom to refuse.
- Network effects: the value and social necessity of a service increase as more people use it.
- Asymmetric relationship: the service sets terms unilaterally, while users have little bargaining power.

Limitations:

- "Just do not use it" is weak when a service is socially or professionally necessary.
- Legal consent text does not ensure genuine user understanding.

Interview hook: Consent is not just a checkbox. Ask whether refusal is realistic, informed, and free from penalty.

## 9. Privacy and Use of Data

Source: PDF 616-618; pp. 592-594

- The chapter rejects the claim that privacy is dead.
- Privacy does not mean total secrecy. It means the ability to choose what to reveal, to whom, in which context, and for what purpose.
- Privacy is framed as a decision right and a component of freedom and autonomy.
- Example: a person with a rare medical condition may willingly share data with researchers to help develop treatment, but may not want that data used by insurers or employers.
- Surveillance infrastructure does not necessarily destroy privacy rights; it transfers practical control from individuals to data collectors.
- Companies effectively ask users to trust them with the power to decide future use of personal data.
- Companies often keep surveillance-derived insights secret because revealing them would appear intrusive and could damage the business model.
- Ad targeting can reveal sensitive group membership indirectly, even if no individual is explicitly reidentified.
- Once companies decide what to infer and reveal, the user's agency over disclosure is weakened.
- Companies often manage perceptions of creepiness rather than addressing how intrusive collection is.
- Data can be wrong, undesirable, inappropriate, or emotionally harmful. Systems need mechanisms for handling those failures.
- Algorithms do not naturally understand human categories such as inappropriate or painful; engineers must explicitly build systems that respect human needs.
- Privacy settings that control what other users can see are only a partial solution. The service itself may still have broad internal access and processing rights.
- Large-scale transfer of privacy control from individuals to corporations is historically unusual because surveillance used to be expensive and manual, but now it is automated and scalable.
- Traditional trusted relationships, such as doctor-patient or attorney-client, have strict ethical, legal, and regulatory constraints. Many internet services collect sensitive data without comparable user understanding or constraint.

Terminology:

- Privacy as decision right: the right to decide how much to reveal in each context.
- Agency: a person's practical control over disclosure and use of information about themselves.

Tradeoff:

- Sharing data can enable valuable outcomes, such as medical research, but the same data can harm the person if used in other contexts.

Interview hook: "Privacy is not secrecy" is the key line of reasoning. The central issue is contextual control.

## 10. Data as Assets and Power

Source: PDF 618-619; pp. 594-595

- Behavioral data is sometimes called "data exhaust," implying a worthless byproduct that can be profitably reused.
- The chapter argues that this framing is misleading. If targeted advertising funds the service, user activity that generates behavioral data can be understood as a form of labor.
- A stronger critique is that the application may lure users into producing personal information for the surveillance infrastructure.
- Personal data is valuable, as shown by data brokers that secretly buy, aggregate, analyze, and resell personal data, mostly for marketing.
- Startups may be valued by user numbers or "eyeballs," which the chapter equates with surveillance capability.
- Companies want data because it is valuable, but governments may also seek it through secret deals, coercion, legal compulsion, or theft.
- Personal data can be sold when a company goes bankrupt.
- Data is difficult to secure, and breaches happen often enough to be a serious design consideration.
- Critics characterize data as a toxic asset, hazardous material, or uranium-like resource: valuable, but dangerous if mishandled.
- Data collection must be evaluated against the risk that it later falls into the wrong hands.
- Threat scenarios include criminal compromise, hostile intelligence services, insider leaks, new management with different values, and future regimes willing to compel disclosure.
- The chapter stresses that data collection decisions should consider future political environments, not only current ones.
- Information asymmetry creates power: scrutinizing others while avoiding scrutiny gives institutions power over people's lives.
- Even if technology companies are not explicitly seeking political power, the data they hold gives them influence outside public oversight.

Terminology:

- Toxic asset/hazardous material framing: personal data may create liability and harm risk, not just business value.
- Information asymmetry: one party knows a lot about another while remaining opaque itself.

Edge cases:

- Data that seems safe under current management or current government may become dangerous after a breach, acquisition, bankruptcy, or political change.

Interview hook: Data minimization is not just a compliance point; it reduces future attack, leak, coercion, and misuse surface.

## 11. Remembering the Industrial Revolution

Source: PDF 619-620; pp. 595-596

- The chapter compares the information age with the Industrial Revolution.
- The Industrial Revolution produced long-term economic growth and higher living standards, but also severe pollution, unsafe working conditions, cramped housing, harsh labor, and child labor.
- Safeguards such as environmental regulation, workplace safety, child labor laws, and food inspections took time to establish.
- These safeguards increased business costs, but society benefited and few would want to return to the unregulated period.
- The chapter argues that data collection and misuse are similar challenges for the information age.
- A quoted source in the chapter frames data as the information-age pollution problem and privacy protection as the environmental challenge.
- The point is not that technology is bad; it is that powerful technology needs social, legal, and engineering safeguards.

Tradeoff:

- Regulation and safeguards may increase operational cost, but the chapter presents them as necessary for society-wide benefit.

Interview hook: Use the industrial analogy to explain why "innovation" and "guardrails" are not opposites. Unchecked extraction can create externalized harm.

## 12. Legislation and Self-Regulation

Source: PDF 620-621; pp. 596-597

- Data protection laws can help preserve individual rights.
- The chapter references GDPR principles that personal data should be collected for specified, explicit, legitimate purposes and limited to what is necessary for those purposes.
- This data minimization principle conflicts with the big-data habit of maximizing collection, combining datasets, and exploring for unexpected insights.
- Exploration often means using data for unforeseen purposes, which conflicts with the idea that purposes should be specified and explicit at collection time.
- GDPR has affected online advertising, but the chapter says enforcement has been weak and broader industry culture has not changed enough.
- Companies that collect lots of data often oppose regulation as a burden and hindrance to innovation.
- The chapter acknowledges a real tradeoff: sharing sensitive data, such as medical data, creates privacy risks but may also enable better diagnosis and treatment.
- Overregulation can prevent beneficial discoveries, so the balance is difficult.
- The chapter's main prescription is a culture shift in the technology industry:
  - Stop treating users as metrics to optimize.
  - Remember that users are humans deserving respect, dignity, and agency.
  - Self-regulate data practices to maintain trust.
  - Educate users about how their data is used.
  - Allow individuals to retain control over their own data.
  - Do not normalize ubiquitous surveillance as inevitable.
- A practical first step is to avoid retaining data forever, purge data once it is no longer needed, and minimize collection in the first place.
- Data not collected cannot be leaked, stolen, or compelled by governments.
- The chapter states that considering societal impact is part of the job of people working in technology.

Terminology:

- Data minimization: collecting and retaining only data needed for specified purposes.
- Purpose limitation: collecting data for explicit legitimate purposes and not reusing it incompatibly.
- Self-regulation: voluntary industry or organizational restraint beyond minimum legal compliance.

Tradeoff:

- Data collection can enable public benefit, but it also creates privacy, misuse, and power risks. The answer is not simply "collect everything" or "collect nothing"; it is careful purpose, minimization, transparency, and accountability.

Interview hook: A pragmatic privacy answer should include deletion, minimization, user education, and explicit purpose, not only encryption or access control.

## 13. Summary and Book Closure

Source: PDF 621-622; pp. 597-598

- The book closes by briefly recapping all previous chapters:
  - Chapter 1: operational versus analytical systems, cloud versus self-hosting, distributed versus single-node systems, and balancing business and user needs.
  - Chapter 2: nonfunctional requirements such as performance, reliability, scalability, and maintainability.
  - Chapter 3: data models and query languages, including relational, document, graph, event sourcing, DataFrames, SQL, Cypher, SPARQL, Datalog, and GraphQL.
  - Chapter 4: OLTP and analytical storage engines, including LSM-trees, B-trees, column storage, full-text search, and vector search.
  - Chapter 5: encoding, evolution, and dataflow through databases, service calls, workflow engines, and event-driven architectures.
  - Chapter 6: replication tradeoffs and consistency models, including offline sync.
  - Chapter 7: sharding, rebalancing, routing, and secondary indexing.
  - Chapter 8: transactions, durability, isolation levels, serializability, and distributed atomicity.
  - Chapter 9: distributed systems faults, clocks, pauses, crashes, and locks.
  - Chapter 10: consensus and linearizability.
  - Chapter 11: batch processing from Unix tools to distributed batch processors.
  - Chapter 12: stream processing, message brokers, CDC, fault tolerance, and streaming joins.
  - Chapter 13: streaming-systems philosophy for integration, evolution, and scaling.
- Chapter 14's final message is that data can do good but can also cause significant harm:
  - Consequential decisions can be difficult to appeal.
  - Data systems can drive discrimination and exploitation.
  - Surveillance can become normalized.
  - Intimate information can be exposed.
  - Data breaches and unintended consequences remain persistent risks.
- Engineers carry responsibility for shaping systems that treat people with humanity and respect.

Interview hook: DDIA ends by connecting technical architecture to human consequences. The strongest engineering decisions include both system correctness and social impact.

## Chapter-Level Memory Hooks

- Data is often about people, not abstract rows.
- Ethics is process, dialogue, and accountability, not a compliance checklist.
- Predictive analytics shifts power when risk labels affect life opportunities.
- Biased historical data can be laundered into apparently objective model output.
- Proxy variables can recreate protected-trait discrimination.
- Statistical truth at the population level can still be wrong for an individual.
- Recourse matters: people need ways to understand, appeal, and correct decisions.
- Feedback loops turn model outputs into future model inputs.
- Privacy means contextual control, not total secrecy.
- Consent is weak without understanding and realistic freedom to refuse.
- Surveillance is a relationship of power, not just data collection.
- Personal data is valuable and hazardous; minimize what you collect and retain.
- Data not collected cannot be leaked, stolen, sold, or compelled.
- The information age needs safeguards the way the industrial age needed environmental and labor protections.

## Interview Perspective

- When asked about ethical ML, start with affected people, not model metrics.
- Discuss false positives and false negatives in terms of who bears harm.
- Mention proxy discrimination: removing protected attributes is insufficient.
- Ask whether the model is explainable enough for appeal, correction, or judicial review.
- Bring up feedback loops in recommender systems, pricing systems, fraud systems, credit scoring, and hiring filters.
- For privacy design, go beyond encryption. Include data minimization, deletion, purpose limitation, consent quality, internal use, inference risk, and user agency.
- For product tradeoffs, distinguish legitimate feature-improving telemetry from surveillance for advertising or unrelated secondary use.
- For architecture reviews, ask: What data are we collecting? Why? For how long? Who can access it? What can be inferred? What happens after acquisition, breach, bankruptcy, or legal compulsion?

## Final Takeaways

- Chapter 14 is DDIA's ethical conclusion: building reliable, scalable, maintainable systems is not enough if the systems harm people.
- Predictive analytics can improve decisions, but it can also automate exclusion, amplify historical bias, and make mistakes difficult to challenge.
- Privacy is about agency over disclosure and use, not about hiding everything.
- Consent is meaningful only when users understand the processing and can realistically refuse or withdraw.
- Data should be treated as both valuable and dangerous. Collection creates future obligations and risks.
- The practical engineering response is minimization, deletion, transparency, accountability, appeal mechanisms, and humility about unintended consequences.

Confidence: High
