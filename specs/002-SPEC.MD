<specification>
  <task>
    Le but est de poursuivre de bout en bout le projet de groupe de Mathias CHEBBAH, Lamine BETRAOUI, BOUNA SALL et Hugues BARBIER, étudiants en M1 MIAGE, pour le cours d'Artificial Intelligence and Reasoning, par toi, en totale autonomie. Le projet est déjà bien entamé. Il faut maintenant améliorer la CLI benchmark, extraire un maximum de données pertinentes sur les algorithmes, puis intégrer proprement ces nouvelles données de simulation dans le rapport et la présentation. Voir @Lab 1 - Solveur Rubik's Cube (Astar).pdf et @README.MD.
  </task>

<teachers_mail> <content>
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
Groups will present one after another according to the schedule that will be communicated later. </content>
</teachers_mail>

  <instructions>
    <step id="0">
      Vérifie que le repo est déjà initialisé, que le projet est bien entamé, et que le repo est propre avant de commencer. On utilisera ça pour tracker notre travail. Vérifie le .gitignore, mais n'ajoute pas le .pdf dedans, c'est important qu'il puisse être stagé. Vérifie l'environnement de développement existant. Le projet utilise Java et Maven, conformément au README et au code déjà produit.
    </step>

```
<step id="1">
  Relis tous les cours dans /Users/mathias/Desktop/Cours/ai_reasoning/, les "Cours" et les "TD". Il faut que tu les relises au maximum dans ton contexte, pour en comprendre le sens. Pourquoi pas avec des subagents ? Car tu as besoin de comprendre comment fonctionnent chaque cours, la pédagogie du professeur, pour rester dans le scope. Ensuite, relis @Lab 1 - Solveur Rubik's Cube (Astar).pdf. C'est l'énoncé du projet que l'on doit améliorer et finaliser.
</step>

<step id="2.0">
  Après avoir relu le sujet, le README et le code existant, découpe le travail restant en petites étapes. Le but est que tu comprennes le découpage logique de l'énoncé, chaque gate, pour ensuite construire des boucles de vérification.
</step>

<step id="2.1">
  Analyse le scaffolding actuel du projet. Le but est de conserver une codebase modulaire, feature-first, où chaque fichier répond à l'énoncé. L'énoncé mentionne des classes particulières à créer, tiens-toi y. Essaye de coller le plus possible à l'énoncé et au projet déjà implémenté.
</step>

<step id="2.2">
  Améliore par étapes la CLI benchmark. Voir @README.MD. Le but est d'obtenir plus de données de simulation, en lisant le code pour identifier les données pertinentes, et d'extraire un maximum de données utiles sur les algorithmes. Pour chaque amélioration, crée ou adapte les tests d'abord, fais passer le rouge, ensuite implémente, vérifie que c'est vert. Progresse itérativement. Commite à chaque sous-étape.
</step>

<step id="2.3">
  Teste tout le code à la fin. Vérifie la compilation, les tests unitaires, le packaging Maven, l'interface graphique si nécessaire, et la CLI benchmark avec plusieurs configurations.
</step>

<step id="2.4">
  Fais des boucles d'itérations. Appelle /code-review, et /codex:review pour vérifier chaque classe et chaque amélioration significative de la CLI benchmark.
</step>

<step id="2.5">
  Modifie le rapport, comme demandé par le professeur, en anglais, en LaTeX. Il doit suivre les report_instructions. Ajoute Hugues Barbier aux auteurs. Insère proprement les nouvelles données de simulation dans le rapport, avec des tableaux, des figures et une analyse claire des résultats.
</step>

<step id="2.6">
  Modifie la présentation minimale en LaTeX, en anglais, qui présente le projet, qui l'explique, avec des schémas, et, le cas échéant, des formules si besoin. Voir presentation_instructions. Ajoute Hugues Barbier aux auteurs. Réorganise la présentation pour que chaque personne ait deux slides. Cela fait exactement 8 slides de contenu, sans compter la slide 1 de couverture. Insère les nouvelles données de simulation dans les slides si possible.
</step>

<step id="2.7">
  Vérifie en boucle les rendus, règle par règle.
</step>
```

  </instructions>

<report_instructions> <instruction id="1">Pas de em-dashes.</instruction> <instruction id="2">Le texte rentre dans la page. En général, une bonne règle est que une fois que tu as fini d'écrire la page, réduis tout le contenu de -20% en taille.</instruction> <instruction id="3">Less is more, une phrase a maximum 15 mots.</instruction> <instruction id="4">Des formulations simples en anglais, naturelles. Il faut prendre en compte que les auteurs sont de niveau B2.</instruction> <instruction id="5">Exactement 12 pages, sans compter la couverture.</instruction> <instruction id="6">Ne pas ajouter de mots, théories, formulations, noms qui ne sont pas explicitement dans le cours.</instruction> <instruction id="7">Ajouter Mathias CHEBBAH, Lamine BETRAOUI, Bouna SALL et Hugues BARBIER comme auteurs.</instruction> <instruction id="8">Insérer proprement les nouvelles données de simulation extraites avec la CLI benchmark.</instruction>
</report_instructions>

<presentation_instructions> <instruction id="1">Pas de em-dashes.</instruction> <instruction id="2">Le texte rentre dans la slide. En général, une bonne règle est que une fois que tu as fini d'écrire la slide, réduis tout le contenu de -20% en taille.</instruction> <instruction id="3">Less is more. On vise pas plus de 3-4 phrases par slide. Une phrase a maximum 15 mots.</instruction> <instruction id="4">Le style beamer latex bleu.</instruction> <instruction id="5">Des formulations simples en anglais, naturelles. Il faut prendre en compte que les orateurs sont de niveau B2.</instruction> <instruction id="6">Exactement 8 transparents de contenu, sans compter la couverture.</instruction> <instruction id="7">Chaque intervenant couvre deux transparents.</instruction> <instruction id="8">Ne pas ajouter de mots, théories, formulations, noms qui ne sont pas explicitement dans le cours.</instruction> <instruction id="9">Ajouter Mathias CHEBBAH, Lamine BETRAOUI, Bouna SALL et Hugues BARBIER comme auteurs.</instruction> <instruction id="10">Insérer les nouvelles données de simulation dans les slides si possible.</instruction>
</presentation_instructions>

  <loops>
    <content>
Crucial : Je porte ton attention particulière sur les loops de vérification.
Créé des loops où tu te promptes toi-même, en invoquant /loop 30 minutes ou /loop 20 minutes, pour suivre le travail.

Tu dois utiliser cette méthodologie pour réussir à faire un travail longue durée. Comme demandé dans les instructions CLAUDE.MD globale, tu dois fréquemment utiliser date. Cela a pour but de te donner conscience du temps, et aussi te faire comprendre que tu vas devoir travailler durant de longues heures.

Tu dois par conséquent programmer des boucles de réveil au sein de ton harness, pour t'assurer que tu continueras à travailler longtemps.

Ainsi, je ne te prompte qu'une fois dans ce document SPEC.MD. Ensuite, tu devras prendre le relais, et exécuter des boucles qui vérifient plusieurs fois le travail, jusqu'à perfection, et satisfaction de mon intent, qui nous permettra d'avoir la note maximale. Tu dois relire plusieurs fois SPEC.MD au cours du temps, et du travail, pour vérifier que tu ne dérives pas. Tu ne saurais te contenter que d'une seule lecture. </content> </loops>

<user_is_afk> <content>
Comme tu es en autonomie, tu ne dois pas me demander mon aide. Si tu es bloqué, débloque-toi en choisissant la solution la plus logique. Arbitre. Je ne regarderai pas la session, je reviendrai plus tard, tu as le temps et je reviendrai dans plusieurs heures. Mathias is afk, go ahead and push. </content>
</user_is_afk> </specification>
