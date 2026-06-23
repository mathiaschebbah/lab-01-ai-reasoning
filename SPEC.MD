<task>
Le but est de générer de bout en bout le projet de groupe de Mathias CHEBBAH, Lamine BETRAOUI, BOUNA SALL, Étudiants en M1 MIAGE, pour le cours d'Artificial Intelligence and Reasoning, par toi, en totale autonomie. (voir) @Lab 1 - Solveur Rubik's Cube (Astar).pdf.
</task>

<teachers_mail>
Voici un mail du prof : 

Dear Students,

I would like to remind you of the requirements for the project submission and the organization of the final presentations.

Project Submission
Please submit your project as a ZIP file to my Paris Dauphine email address by end of Thursday. 

The ZIP file must contain:

A PDF report explaining your project, including its objectives, methodology, and results.
The complete source code of your project.
Please ensure that all required files are included before submission.

Project Presentations
Each group will have 15 minutes in total:

7–8 minutes for the presentation.
7–8 minutes for questions and answers.
Groups will present one after another according to the schedule that will be communicated later.
</teachers_mail>

<instructions>
0. Initialise le repo si pas déjà fait, et vérifie que le repo est propre. On utilisera ça pour tracker notre travail. Spin up un .gitignore, mais n'ajoute pas, le .pdf, c'est important qu'il soit stagé. Initialise l'environnement de développement. Mes camarades et moi hésitons sur le choix du langage, (à priori ça se décide entre Python et Java, tranche, suis le langage qui se rapproche le plus de l'énoncé, et qui aboutierait au meilleur résultat)
1. Lis tous les cours dans /Users/mathias/Desktop/Cours/ai_reasoning/, les "Cours" et les "TD". Il faut que tu les lises au maximum dans ton contexte, pour en comprendre le sens.
Pourquoi pas avec des subagents ? Car tu as besoin de comprendre comment fonctionnent chaque cours, la pédagogie du professeur, pour rester dans le scope. Ensuite, lis @Lab 1 - Solveur Rubik's Cube (Astar).pdf. C'est l'énoncé du projet que l'on doit réaliser.
2.0 Après avoir lu le sujet, découpe le travail en petites étapes (voir 2.1). Le but est que tu comprennes le découpage logique de l'énoncé, chaque gate, pour ensuite construire des boucles de vérification (voir 2.2, 2.3)
2.1. Créé le scaffolding du projet. Le but est d'avoir une codebase modulaire, feature-first, où chaque fichier répond à l'énoncé. L'énoncé mentionne des classes particulières à créer, tiens toi y. Essaye de coller le plus possible à l'énoncé.
2.2. Implémente par étapes, pour arriver à l'implémentation de bout en bout. Pour chaque classe, créé les tests d'abord, fais passer le rouge, ensuite, implémente, vérifie que c'est vert. Progresse itérativement. Commite à chaque sous étape.
2.3. Teste ton code à la fin.
2.4. Fais des boucles d'itérations. Appelle /code-review, et /codex:review pour vérifier chaque classe.
2.5. Créé un rapport, comme demandé par le professeur en anglais, en LaTeX. Il doit suivre les <report_instructions>. 
2.6. À la fin, créé une présentation minimale en LaTeX, en anglais, qui présente le projet, qui l'explique, avec des schémas, et, le cas échéant, des formules si besoin. (voir <presentation_instructions>)
2.7. Vérifie en boucle les rendus, règle par règle.

<presentation_instructions>
Pour que le rapport soit conforme, elle doit respecter ces critères.
1. Pas de em-dashes.
2. Le texte rentre dans la page. En général, une bonne règle est que une fois que tu as fini d'écrire la slide, réduis tout le contenu de -20% en taille.
3. Less is more, une phrase a maximum 15 mots.
6. Des formulations simples en anglais, naturelles. Il faut prendre en compte que les orateurs sont de niveau B2.
7. Exactement 12 pages (sans compter la couverture).
8. Ne pas ajouter de mots, théories, formulations, noms qui ne sont pas explicitement dans le cours.
</presentations_instructions>

<presentation_instructions>
Pour que la présentation soit conforme, elle doit respecter ces critères.
1. Pas de em-dashes.
2. Le texte rentre dans la slide. En général, une bonne règle est que une fois que tu as fini d'écrire la slide, réduis tout le contenu de -20% en taille.
3. Less is more. On vise pas plus de 3-4 phrases par slide. Une phrase a maximum 15 mots.
4. Le style beamer latex bleu.
6. Des formulations simples en anglais, naturelles. Il faut prendre en compte que les orateurs sont de niveau B2.
7. Exactement 9 transparents (sans compter la couverture), idée est que 3*3=9, chaque intervenant couvre un transparent.
8. Ne pas ajouter de mots, théories, formulations, noms qui ne sont pas explicitement dans le cours.
</presentations_instructions>

<loops>
Crucial : Je porte ton attention particulière sur les loops de vérification.
Créé des loops où tu te promptes toi même, en invoquant /loop 30 minutes (ou 20 minutes), pour suivre le travail.

Tu dois utiliser cette méthodologie pour réussir à faire un travail longue durée. Comme demandé dans les instructions CLAUDE.MD globale, tu dois fréquemment utiliser 'date'. Cela a pour but de te donner conscience du temps, et aussi te faire comprendre que tu vas devoir travailler durant de longues heures.
Tu dois par conséquent programmer des boucles de réveil au sein de ton harness, pour t'assurer que tu continueras à travailler longtemps.
Ainsi, je ne te prompte qu'une fois (dans ce document SPEC.MD), ensuite, tu devrass prendre le relais, et exécuter des boucles qui vérifient plusieurs fois le travail, jusqu'à perfection, et satisfaction de mon intent, qui nous permettera d'avoir la note maximale. Tu dois relire plusieurs fois SPEC.MD au cours du temps, et du travail, pour vérifier que tu ne dérives pas, tu ne saurais te contenter que d'une seule lecture.
</loops>

<user_is_afk>
Comme tu es en autonomie, tu ne dois pas me demander mon aide. Si tu es bloqué, débloque toi en choissisant la solution la plus logique. Arbitre. Je ne regarderais pas la session, je reviendrais plus tard, tu as le temps et je reviendrais dans plusieurs heures. Mathias is afk, go ahead and push.
<user_is_afk>

</instructions>
