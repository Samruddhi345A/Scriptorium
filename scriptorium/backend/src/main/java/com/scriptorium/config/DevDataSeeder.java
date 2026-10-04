package com.scriptorium.config;

import com.scriptorium.domain.ClusterNode;
import com.scriptorium.domain.ManuscriptNode;
import com.scriptorium.domain.relationships.*;
import com.scriptorium.repository.ClusterRepository;
import com.scriptorium.repository.ManuscriptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Populates Neo4j with two clusters and 8 manuscript nodes on dev startup.
 * Only runs when spring.profiles.active=dev.
 * Safe to re-run — skips seeding if data already exists.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataSeeder implements CommandLineRunner {

    private final ManuscriptRepository manuscriptRepository;
    private final ClusterRepository clusterRepository;

    @Override
    public void run(String... args) {
        if (manuscriptRepository.count() > 0) {
            log.info("Dev seed: data already present, skipping.");
            return;
        }

        log.info("Dev seed: populating Neo4j with sample data...");

        seedHistoriaRegumBritanniae();
        seedCanterburyTales();

        log.info("Dev seed: complete.");
    }

    // ── Cluster 1: Historia Regum Britanniae ──────────────────────────────────

    private void seedHistoriaRegumBritanniae() {
        ClusterNode cluster = new ClusterNode();
        cluster.setName("Historia Regum Britanniae");
        cluster.setDescription(
                "The textual tradition surrounding Geoffrey of Monmouth's foundational chronicle of British kings, "
                + "tracing legendary history from Brutus of Troy to the Arthurian age.");
        cluster.setEra("Medieval");
        cluster.setTradition("Arthurian / British Chronicle");
        clusterRepository.save(cluster);

        // Node 1 — Latin original
        ManuscriptNode latin = manuscript(
                "Historia Regum Britanniae",
                "Historia Regum Britanniae",
                "la", "Caroline minuscule",
                "c. 1138", 1130, 1145,
                "Monmouth, Wales / Oxford",
                "Geoffrey of Monmouth", null,
                "Bodleian Library, University of Oxford",
                "MS Rawlinson B 148",
                "good",
                "The foundational Latin prose chronicle by Geoffrey of Monmouth, purporting to trace the history "
                + "of the kings of Britain from Brutus of Troy through the reign of Cadwaladr. "
                + "Contains the earliest extended narrative of King Arthur.",
                cluster
        );

        // Node 2 — Anglo-Norman French translation (Wace)
        ManuscriptNode french = manuscript(
                "Roman de Brut",
                "Li Romans de Brut",
                "fro", "Gothic textura",
                "c. 1155", 1150, 1165,
                "Normandy, France",
                "Wace", "Wace",
                "Bibliothèque nationale de France",
                "MS fr. 1450",
                "good",
                "Wace's verse adaptation of Geoffrey's Historia into Anglo-Norman French octosyllabic couplets. "
                + "Introduces the Round Table and expands Arthurian material considerably. "
                + "Dedicated to Eleanor of Aquitaine.",
                cluster
        );

        // Node 3 — Middle English translation (Layamon)
        ManuscriptNode english = manuscript(
                "Brut",
                "Brut",
                "enm", "Early English vernacular",
                "c. 1190–1215", 1190, 1215,
                "Worcestershire, England",
                "Layamon", "Layamon",
                "British Library",
                "Cotton MS Caligula A IX",
                "fair",
                "Layamon's Middle English alliterative verse adaptation of Wace's Roman de Brut. "
                + "The earliest surviving major literary work in Middle English. "
                + "Expands the Arthurian legend further, naming the island of Avalon.",
                cluster
        );

        // Node 4 — 14th century Welsh prose adaptation
        ManuscriptNode welsh = manuscript(
                "Brut y Brenhinedd",
                "Brut y Brenhinedd",
                "cy", "Welsh secretary hand",
                "c. 1330–1350", 1330, 1350,
                "Wales",
                null, "Unknown Welsh scribe",
                "National Library of Wales",
                "Peniarth MS 44",
                "fair",
                "A Welsh prose translation of Geoffrey's Historia Regum Britanniae, "
                + "one of several surviving Welsh versions. Integrates the narrative into native Welsh "
                + "historical tradition. Closely follows the Latin source with minor adaptations.",
                cluster
        );

        // Node 5 — 20th century scholarly critical edition
        ManuscriptNode scholarly = manuscript(
                "Historia Regum Britanniae: A Variant Version",
                "Historia Regum Britanniae",
                "en", "Modern typography",
                "1951", 1951, 1951,
                "Cambridge, UK",
                null, "Jacob Hammer (ed.)",
                "Mediaeval Academy of America",
                "Publication No. 57",
                "good",
                "Jacob Hammer's critical edition of the variant version of Geoffrey's Historia, "
                + "establishing the stemma of the manuscript tradition and identifying distinct textual families. "
                + "The standard scholarly reference for the variant recension.",
                cluster
        );

        // Relationships
        TranslatedFrom waceFromGeoffrey = rel(TranslatedFrom.class, latin, "established",
                "Wace explicitly acknowledges Geoffrey's Historia as his source in the prologue.",
                "Weiss, J. (2002). Wace's Roman de Brut. University of Exeter Press.");
        french.getTranslatedFrom().add(waceFromGeoffrey);

        TranslatedFrom layamonFromWace = rel(TranslatedFrom.class, french, "established",
                "Layamon states in his prologue that Wace's French book was one of his three sources.",
                "Brook & Leslie (1963). Layamon: Brut. EETS.");
        english.getTranslatedFrom().add(layamonFromWace);

        TranslatedFrom welshFromLatin = rel(TranslatedFrom.class, latin, "established",
                "The Welsh Brut y Brenhinedd follows the Latin text of Geoffrey closely.",
                "Roberts, B.F. (1971). Brut y Brenhinedd. Dublin Institute for Advanced Studies.");
        welsh.getTranslatedFrom().add(welshFromLatin);

        DerivedFrom scholarlyFromLatin = rel(DerivedFrom.class, latin, "established",
                "Hammer's edition is a critical edition of the Latin variant manuscript tradition.",
                "Hammer, J. (1951). Historia Regum Britanniae: A Variant Version. Mediaeval Academy.");
        scholarly.getDerivedFrom().add(scholarlyFromLatin);

        ParallelTradition welshParallel = rel(ParallelTradition.class, english, "probable",
                "The Welsh and Middle English traditions developed independently from the Latin source "
                + "but share thematic parallels in the treatment of Arthurian material.",
                "Padel, O.J. (2000). Arthur in Medieval Welsh Literature. University of Wales Press.");
        welsh.getParallelTradition().add(welshParallel);

        manuscriptRepository.save(french);
        manuscriptRepository.save(english);
        manuscriptRepository.save(welsh);
        manuscriptRepository.save(scholarly);
    }

    // ── Cluster 2: The Canterbury Tales ──────────────────────────────────────

    private void seedCanterburyTales() {
        ClusterNode cluster = new ClusterNode();
        cluster.setName("The Canterbury Tales");
        cluster.setDescription(
                "The manuscript tradition of Chaucer's Canterbury Tales, comprising over 80 surviving witnesses "
                + "ranging from Chaucer's own time to the early print era.");
        cluster.setEra("Late Medieval");
        cluster.setTradition("Middle English Literary");
        clusterRepository.save(cluster);

        // Node 6 — The Hengwrt manuscript (earliest, c. 1400)
        ManuscriptNode hengwrt = manuscript(
                "The Canterbury Tales (Hengwrt)",
                "The Canterbury Tales",
                "enm", "English cursive (Anglicana formata)",
                "c. 1400–1410", 1400, 1410,
                "London, England",
                "Adam Pinkhurst (attr.)", "Geoffrey Chaucer",
                "National Library of Wales",
                "Peniarth MS 392D",
                "good",
                "The Hengwrt Chaucer is widely regarded as the earliest surviving manuscript of the Canterbury Tales, "
                + "copied shortly after Chaucer's death in 1400, likely by Adam Pinkhurst. "
                + "Despite textual authority, it lacks several tales and has an irregular order.",
                cluster
        );

        // Node 7 — The Ellesmere manuscript (c. 1400–1405, best organised)
        ManuscriptNode ellesmere = manuscript(
                "The Canterbury Tales (Ellesmere)",
                "The Canterbury Tales",
                "enm", "English cursive (Anglicana formata)",
                "c. 1400–1405", 1400, 1405,
                "London, England",
                "Adam Pinkhurst (attr.)", "Geoffrey Chaucer",
                "Huntington Library, San Marino",
                "EL 26 C 9",
                "excellent",
                "The Ellesmere manuscript is the most luxurious and complete early copy of the Canterbury Tales. "
                + "It contains 23 miniature portraits of the pilgrims and is the basis of most modern editions. "
                + "Shares a scribe (Adam Pinkhurst) with the Hengwrt manuscript.",
                cluster
        );

        // Node 8 — Caxton's first printed edition (1476)
        ManuscriptNode caxton = manuscript(
                "The Canterbury Tales (Caxton, First Edition)",
                "The Canterbury Tales",
                "enm", "Early print (Caxton type)",
                "1476", 1476, 1476,
                "Westminster, England",
                null, "Geoffrey Chaucer",
                "British Library",
                "IB.55021",
                "good",
                "William Caxton's first printed edition of the Canterbury Tales, produced at Westminster in 1476. "
                + "One of the earliest books printed in England. Caxton later produced a second edition (1483) "
                + "after a reader supplied a better manuscript, acknowledging errors in this first edition.",
                cluster
        );

        // Relationships
        CopiedFrom ellesmereFromHengwrt = rel(CopiedFrom.class, hengwrt, "probable",
                "Both manuscripts share textual readings and are attributed to the same scribe, Adam Pinkhurst, "
                + "though the exact relationship between them is debated.",
                "Mooney, L.R. (2006). Chaucer's Scribe. Speculum 81(1), 97–138.");
        ellesmere.getCopiedFrom().add(ellesmereFromHengwrt);

        DerivedFrom caxtonFromEllesmere = rel(DerivedFrom.class, ellesmere, "speculative",
                "Caxton's copy text has not been conclusively identified; it may derive from a manuscript "
                + "related to the Ellesmere tradition.",
                "Blake, N.F. (1985). The Textual Tradition of the Canterbury Tales. Arnold.");
        caxton.getDerivedFrom().add(caxtonFromEllesmere);

        manuscriptRepository.save(hengwrt);
        manuscriptRepository.save(ellesmere);
        manuscriptRepository.save(caxton);
    }

    // ── Factory helpers ───────────────────────────────────────────────────────

    private ManuscriptNode manuscript(
            String title, String originalTitle,
            String language, String script,
            String approximateDate, int dateLower, int dateUpper,
            String originRegion, String scribe, String author,
            String holdingInstitution, String shelfmark,
            String condition, String summary,
            ClusterNode cluster) {

        ManuscriptNode node = new ManuscriptNode();
        node.setTitle(title);
        node.setOriginalTitle(originalTitle);
        node.setLanguage(language);
        node.setScript(script);
        node.setApproximateDate(approximateDate);
        node.setDateLower(dateLower);
        node.setDateUpper(dateUpper);
        node.setOriginRegion(originRegion);
        node.setScribe(scribe);
        node.setAuthor(author);
        node.setHoldingInstitution(holdingInstitution);
        node.setShelfmark(shelfmark);
        node.setCondition(condition);
        node.setSummary(summary);
        node.setIsPublished(true);
        node.setCreatedAt(LocalDateTime.now());
        node.setCluster(cluster);
        return manuscriptRepository.save(node);
    }

    @SuppressWarnings("unchecked")
    private <T> T rel(Class<T> type, ManuscriptNode target,
                      String confidence, String description, String scholarlySource) {
        try {
            T rel = type.getDeclaredConstructor().newInstance();
            type.getMethod("setTarget", ManuscriptNode.class).invoke(rel, target);
            type.getMethod("setConfidence", String.class).invoke(rel, confidence);
            type.getMethod("setDescription", String.class).invoke(rel, description);
            type.getMethod("setScholarlySource", String.class).invoke(rel, scholarlySource);
            return rel;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create relationship: " + type.getSimpleName(), e);
        }
    }
}
