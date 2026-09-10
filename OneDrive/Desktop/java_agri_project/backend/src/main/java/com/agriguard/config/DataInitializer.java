package com.agriguard.config;

import com.agriguard.entity.*;
import com.agriguard.repository.CropRepository;
import com.agriguard.repository.DiagnosisRepository;
import com.agriguard.repository.DiseaseRepository;
import com.agriguard.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CropRepository cropRepository;
    private final DiseaseRepository diseaseRepository;
    private final UserRepository userRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(CropRepository cropRepository,
                           DiseaseRepository diseaseRepository,
                           UserRepository userRepository,
                           DiagnosisRepository diagnosisRepository,
                           PasswordEncoder passwordEncoder) {
        this.cropRepository = cropRepository;
        this.diseaseRepository = diseaseRepository;
        this.userRepository = userRepository;
        this.diagnosisRepository = diagnosisRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("Checking and initializing AgriGuard knowledge base and seed data...");

        // 1. Initialize Users
        User adminUser = null;
        if (userRepository.count() == 0) {
            User demo = new User("farmer_john", "john@agriguard.ai", passwordEncoder.encode("agri123"), "John Green", "ROLE_USER");
            adminUser = new User("admin", "admin@agriguard.ai", passwordEncoder.encode("admin123"), "AgriGuard Admin", "ROLE_ADMIN");
            userRepository.saveAll(List.of(demo, adminUser));
            log.info("Seeded default users: farmer_john, admin");
        } else {
            adminUser = userRepository.findByUsername("admin").orElse(null);
        }

        // 2. Initialize Crops
        Map<String, Crop> cropMap = new HashMap<>();
        if (cropRepository.count() == 0) {
            List<Crop> crops = List.of(
                    new Crop("Tomato", "Solanum lycopersicum", "Solanaceae", "tomato_early_blight,tomato_late_blight,tomato_yellow_leaf_curl,tomato_bacterial_spot,tomato_healthy", "Requires full sun (6-8 hours daily), fertile well-draining loam, and sturdy trellising support.", "21-29°C", "6.0-6.8", "Deep watering at base 2-3 times/week. Avoid wetting foliage.", "🍅"),
                    new Crop("Potato", "Solanum tuberosum", "Solanaceae", "potato_early_blight,potato_late_blight,potato_healthy", "Prefers cool conditions, loose well-aerated soil. Mound soil around base as stems grow.", "15-20°C", "5.0-6.0", "Consistent moisture throughout tuber initiation; reduce before harvest.", "🥔"),
                    new Crop("Chili & Bell Pepper", "Capsicum annuum", "Solanaceae", "pepper_bacterial_spot,pepper_anthracnose,pepper_healthy", "Thrives in warm, sunny spots with rich, well-draining soil. Sensitive to cold drafts.", "22-30°C", "6.2-7.0", "Water moderately when top inch dries. Drip irrigation recommended.", "🌶️"),
                    new Crop("Cotton", "Gossypium hirsutum", "Malvaceae", "cotton_bacterial_blight,cotton_bollworm_damage,cotton_healthy", "Warm-season crop requiring prolonged sunshine, deep fertile sandy loam, and regulated nitrogen.", "25-35°C", "5.8-7.5", "High water demand during flowering; minimize water during boll opening.", "🌱"),
                    new Crop("Corn / Maize", "Zea mays", "Poaceae", "corn_northern_leaf_blight,corn_common_rust,corn_healthy", "Heavy feeder needing high nitrogen, full sun, and block planting for wind pollination.", "20-32°C", "5.8-7.0", "Needs 1-1.5 inches water/week, critical during silking and tasseling.", "🌽"),
                    new Crop("Apple", "Malus domestica", "Rosaceae", "apple_scab,apple_black_rot,apple_healthy", "Requires winter chilling hours, well-drained orchard loam, and annual dormant pruning.", "15-24°C", "6.0-7.0", "Deep watering every 7-10 days for young trees. Mulch root zone.", "🍎"),
                    new Crop("Grape", "Vitis vinifera", "Vitaceae", "grape_black_rot,grape_powdery_mildew,grape_healthy", "Requires well-drained gravelly slopes, full sun exposure, and canopy training.", "20-30°C", "5.5-6.5", "Deep, infrequent watering with drip lines under trellis.", "🍇"),
                    new Crop("Rice", "Oryza sativa", "Poaceae", "rice_blast,rice_healthy", "Grown under flooded paddy or aerobic upland systems; requires high temperatures.", "22-34°C", "5.5-6.5", "Continuous shallow flooding or alternate wetting and drying.", "🌾"),
                    new Crop("Wheat", "Triticum aestivum", "Poaceae", "wheat_leaf_rust,wheat_healthy", "Cool-season cereal requiring firm seedbed and timely top-dressing.", "15-25°C", "6.0-7.0", "Critical irrigation stages: crown root initiation and grain filling.", "🌾"),
                    new Crop("Citrus", "Citrus spp.", "Rutaceae", "citrus_canker,citrus_healthy", "Subtropical tree needing well-aerated sandy loam and rich micronutrients.", "20-32°C", "5.5-6.5", "Water deeply when top 2 inches dry. Avoid water ponding.", "🍊"),
                    new Crop("Rose", "Rosa spp.", "Rosaceae", "rose_black_spot,rose_powdery_mildew,rose_healthy", "Ornamental perennial thriving in rich compost-amended soil with 6+ hours of sun.", "18-28°C", "6.0-6.5", "Water at soil level in early morning. Avoid overhead sprinklers.", "🌹"),
                    new Crop("Strawberry", "Fragaria × ananassa", "Rosaceae", "strawberry_leaf_scorch,strawberry_healthy", "Shallow-rooted perennial needing raised beds, pine needle mulch, and clean weed-free soil.", "18-26°C", "5.5-6.5", "Keep top 2 inches consistently moist; use drip lines under straw.", "🍓"),
                    new Crop("Soybean", "Glycine max", "Fabaceae", "soybean_rust,soybean_healthy", "Legume fixing atmospheric nitrogen; needs warm seedbed and moisture at pod fill.", "20-30°C", "6.0-6.8", "Irrigate during pod elongation and seed filling.", "🫘"),
                    new Crop("Coffee", "Coffea arabica", "Rubiaceae", "coffee_leaf_rust,coffee_healthy", "Thrives in tropical highlands with volcanic soil, filtered shade, and distinct wet/dry seasons.", "18-24°C", "5.0-6.0", "High moisture during flowering; slight dry spell to induce uniform blossoming.", "☕"),
                    new Crop("Cucumber", "Cucumis sativus", "Cucurbitaceae", "cucumber_downy_mildew,cucumber_powdery_mildew,cucumber_healthy", "Fast-growing vining crop requiring warm soil, high organic matter, and vertical trellises.", "22-30°C", "6.0-6.8", "Frequent, even watering (1 inch/week). Irregular watering causes bitterness.", "🥒")
            );
            List<Crop> savedCrops = cropRepository.saveAll(crops);
            for (Crop c : savedCrops) {
                cropMap.put(c.getName(), c);
            }
            log.info("Seeded {} agricultural crops into knowledge base.", savedCrops.size());
        } else {
            for (Crop c : cropRepository.findAll()) {
                cropMap.put(c.getName(), c);
            }
        }

        // 3. Initialize Diseases
        if (diseaseRepository.count() == 0) {
            Crop tomato = cropMap.get("Tomato");
            Crop potato = cropMap.get("Potato");
            Crop pepper = cropMap.get("Chili & Bell Pepper");
            Crop cotton = cropMap.get("Cotton");
            Crop corn = cropMap.get("Corn / Maize");
            Crop apple = cropMap.get("Apple");
            Crop grape = cropMap.get("Grape");
            Crop rice = cropMap.get("Rice");
            Crop wheat = cropMap.get("Wheat");

            List<Disease> diseases = List.of(
                    // Tomato
                    new Disease("tomato_early_blight", "Early Blight (Alternaria solani)", tomato, CategoryEnum.FUNGAL,
                            "Common fungal pathogen affecting foliage, stems, and fruit during warm humid weather.",
                            "Concentric ring spots (bullseye pattern) on lower leaves, yellow chlorotic halos, premature leaf drop.",
                            "Prune lower infected leaves immediately. Apply copper-based or bio-fungicides every 7-10 days.",
                            "Maintain wide plant spacing, stake plants, drip irrigate base, mulch soil, and rotate crops 3 years.",
                            "LOW: <10% lower leaves affected. MEDIUM: 10-35% canopy with stem lesions. HIGH: >35% defoliation.",
                            "Spray Bacillus subtilis, neem oil extract, or copper octanoate.",
                            "Chlorothalonil, Mancozeb, or Azoxystrobin following label harvest intervals.", false),

                    new Disease("tomato_late_blight", "Late Blight (Phytophthora infestans)", tomato, CategoryEnum.FUNGAL,
                            "Destructive water-mold pathogen causing rapid canopy collapse during cool, wet weather.",
                            "Water-soaked dark lesions, white fungal fuzz on leaf undersides in high humidity, brown rot on fruit.",
                            "Uproot severely diseased plants immediately in sealed bags. Spray systemic protective fungicides.",
                            "Use certified disease-free transplants, avoid overhead watering, ensure maximum airflow.",
                            "LOW: Few leaf tip lesions. MEDIUM: Multiple stems affected. HIGH: Rapid systemic collapse.",
                            "Preventive copper sulfate sprays or potassium phosphite biostimulants.",
                            "Metalaxyl-M, Dimethomorph, or Mandipropamid.", false),

                    new Disease("tomato_yellow_leaf_curl", "Tomato Yellow Leaf Curl Virus (TYLCV)", tomato, CategoryEnum.VIRAL,
                            "Geminivirus transmitted primarily by the silverleaf whitefly, causing severe stunting.",
                            "Upward curling of leaflet margins, interveinal chlorosis, stunted bushy growth, flower drop.",
                            "No chemical cure exists for virus. Remove infected plants to prevent whitefly vector spread.",
                            "Install yellow sticky traps, use reflective silver mulches, grow TYLCV-resistant hybrids.",
                            "LOW: Mild leaf curling on top shoot. MEDIUM: Moderate stunting. HIGH: Total flower abortion.",
                            "Insecticidal soap, cold-pressed neem oil, or Beauveria bassiana for vector control.",
                            "Imidacloprid, Acetamiprid, or Spiromesifen to control whitefly vector.", false),

                    new Disease("tomato_bacterial_spot", "Bacterial Spot (Xanthomonas spp.)", tomato, CategoryEnum.BACTERIAL,
                            "Bacterial infection causing necrotic spots on foliage and scabby raised lesions on fruit.",
                            "Small 2-3mm dark brown water-soaked spots with yellow halos; spots coalesce into leaf scorch.",
                            "Avoid handling wet plants. Spray copper bactericides mixed with mancozeb for synergy.",
                            "Use hot-water treated seeds, sanitize tools, avoid sprinkler irrigation, rotate crops.",
                            "LOW: Sparse spots on lower leaves. MEDIUM: Moderate spotting. HIGH: Heavy defoliation.",
                            "Fixed copper hydroxide + Bacillus amyloliquefaciens.",
                            "Copper Hydroxide + Mancozeb tank mix or Streptomycin where permitted.", false),

                    new Disease("tomato_healthy", "Healthy Tomato Plant", tomato, CategoryEnum.UNKNOWN,
                            "Plant displays vigorous, balanced vegetative and reproductive growth with deep green foliage.",
                            "Uniform green color, sturdy erect stems, clean flower clusters, firm blemish-free fruit.",
                            "Maintain current watering, feeding, and pest monitoring routine.",
                            "Continue balanced N-P-K fertilization, sucker pruning, staking, and scouting.",
                            "LOW: Optimal vitality.",
                            "Compost tea drenches and beneficial microbial inoculants.",
                            "No chemical intervention needed.", false),

                    // Potato
                    new Disease("potato_early_blight", "Early Blight (Alternaria solani)", potato, CategoryEnum.FUNGAL,
                            "Fungal leaf spot reducing photosynthesis and prematurely aging potato vines.",
                            "Dark brown concentric target rings on older leaves, yellowing margins, dry leathery spots.",
                            "Apply foliar protective fungicides at first sighting. Ensure adequate potassium nutrition.",
                            "Plant certified seed potatoes, maintain spacing, avoid overhead sprinkler watering.",
                            "LOW: Few lower leaves spotted. MEDIUM: 20% vine coverage. HIGH: Extensive vine dieback.",
                            "Copper hydroxide or Trichoderma harzianum foliar sprays.",
                            "Mancozeb, Difenoconazole, or Boscalid.", false),

                    new Disease("potato_late_blight", "Late Blight (Phytophthora infestans)", potato, CategoryEnum.FUNGAL,
                            "Historic pathogen infecting foliage, stems, and tubers causing rapid tissue collapse.",
                            "Pale green water-soaked spots turning purplish-black with fuzzy white growth underneath.",
                            "Destroy infected vines before harvest. Apply protective and curative fungicides immediately.",
                            "Hill soil high to shield tubers from spore wash-in, destroy cull piles, monitor forecasts.",
                            "LOW: Spotting on 1-2 plants. MEDIUM: Spreading across canopy. HIGH: Field-wide collapse.",
                            "Copper octanoate and bio-fungicides.",
                            "Cymoxanil + Mancozeb, Fluopicolide, or Propamocarb.", false),

                    new Disease("potato_healthy", "Healthy Potato Plant", potato, CategoryEnum.UNKNOWN,
                            "Dense, dark green foliage with sturdy vine structure and healthy root/tuber development.",
                            "Vibrant green leaves, upright bushy vines, absence of wilting or necrotic spots.",
                            "Continue optimal irrigation and monitor for Colorado potato beetles.",
                            "Maintain soil hilling, moderate nitrogen, and consistent moisture.",
                            "LOW: Optimal vitality.",
                            "Organic kelp meal and bio-stimulants.",
                            "No chemicals needed.", false),

                    // Chili / Pepper
                    new Disease("pepper_bacterial_spot", "Bacterial Spot (Xanthomonas campestris)", pepper, CategoryEnum.BACTERIAL,
                            "Common bacterial disease of peppers causing defoliation and sunscalded fruit.",
                            "Small water-soaked dark green spots turning brown with yellow borders; foliage drops readily.",
                            "Strip severely infected foliage. Apply copper bactericides. Avoid working in wet fields.",
                            "Use certified disease-free seed, dip seeds in warm water before sowing, avoid overhead water.",
                            "LOW: Isolated lower spots. MEDIUM: Moderate defoliation. HIGH: Severe defoliation.",
                            "Copper soap / Copper octanoate + Bacillus subtilis.",
                            "Copper Hydroxide + Kasugamycin or Mancozeb.", false),

                    new Disease("pepper_anthracnose", "Anthracnose (Colletotrichum spp.)", pepper, CategoryEnum.FUNGAL,
                            "Fungal disease causing circular sunken lesions on pepper fruit and necrotic leaf spots.",
                            "Water-soaked circular lesions on fruit, becoming sunken with pink/orange spore rings.",
                            "Harvest and discard symptomatic fruit immediately. Spray protective fungicides.",
                            "Mulch beds to prevent soil splash, rotate crops, space plants for quick canopy drying.",
                            "LOW: Spots on 1-2 fruits. MEDIUM: 15% crop loss. HIGH: >40% fruit rot.",
                            "Neem oil, potassium bicarbonate, or copper fungicides.",
                            "Azoxystrobin, Pyraclostrobin, or Chlorothalonil.", false),

                    new Disease("pepper_healthy", "Healthy Chili & Pepper Plant", pepper, CategoryEnum.UNKNOWN,
                            "Robust bushy plant with glossy deep green foliage, white flowers, and firm peppers.",
                            "Vibrant foliage without curling or spotting; strong branch structure.",
                            "Maintain balanced feeding and steady soil moisture.",
                            "Scout regularly for aphids/thrips; maintain mulch layer.",
                            "LOW: Optimal vitality.",
                            "Compost dressings and bio-fertilizers.",
                            "No chemicals needed.", false),

                    // Cotton
                    new Disease("cotton_bacterial_blight", "Bacterial Blight / Angular Leaf Spot", cotton, CategoryEnum.BACTERIAL,
                            "Bacterial disease affecting leaves, stems, and bolls in warm, wet conditions.",
                            "Angular dark brown/black water-soaked lesions bounded by veins; blackarm stem lesions.",
                            "Destroy crop residues after harvest. Apply copper-based bactericides.",
                            "Plant resistant cultivars, acid-delint seed before planting, avoid flood over-irrigation.",
                            "LOW: Angular spots on lower leaves. MEDIUM: Lesions extending to stems. HIGH: Blackarm girdling.",
                            "Copper oxychloride sprays and plant defense activators.",
                            "Streptomycin Sulfate or Copper Hydroxide formulations.", false),

                    new Disease("cotton_bollworm_damage", "Bollworm Pest Damage (Helicoverpa)", cotton, CategoryEnum.PEST,
                            "Caterpillar pest boring into flower squares and developing bolls.",
                            "Chewed holes in buds and bolls with visible frass; flared squares and premature drop.",
                            "Handpick caterpillars if small plot. Release Trichogramma wasps or apply Bt sprays.",
                            "Scout twice weekly for eggs, grow marigold trap crops, plant Bt cotton hybrids.",
                            "LOW: <5% square damage. MEDIUM: 5-15% damage. HIGH: >15% square/boll infestation.",
                            "Bacillus thuringiensis (Bt) sprays, Neem oil (Azadirachtin), Spinosad.",
                            "Emamectin Benzoate, Chlorantraniliprole, or Flubendiamide.", false),

                    new Disease("cotton_healthy", "Healthy Cotton Stand", cotton, CategoryEnum.UNKNOWN,
                            "Vigorous growth with strong main stem, healthy dark green leaves, and clean bolls.",
                            "Erect structure, absence of leaf curling, strong square retention.",
                            "Maintain balanced nutrition and soil moisture management.",
                            "Continue IPM scouting and pheromone trap monitoring.",
                            "LOW: Optimal vitality.",
                            "Organic fish amino acid foliar sprays.",
                            "No chemicals needed.", false),

                    // Corn / Maize
                    new Disease("corn_northern_leaf_blight", "Northern Corn Leaf Blight", corn, CategoryEnum.FUNGAL,
                            "Fungal disease causing large elliptical grayish-green or tan lesions that reduce grain fill.",
                            "Cigar-shaped, elongated tan/gray lesions (2-15 cm) running parallel to leaf veins.",
                            "Apply foliar fungicides at tasseling stage if weather is cool and humid.",
                            "Plant resistant hybrids, till previous crop debris, rotate crops for at least 1 year.",
                            "LOW: Few lesions on bottom leaves. MEDIUM: Lesions reaching ear leaf. HIGH: Blight covering canopy.",
                            "Trichoderma biocontrol and copper formulations.",
                            "Pyraclostrobin + Fluxapyroxad, Propiconazole, or Azoxystrobin.", false),

                    new Disease("corn_common_rust", "Common Rust (Puccinia sorghi)", corn, CategoryEnum.FUNGAL,
                            "Airborne fungal rust pathogen creating powdery pustules on foliage.",
                            "Cinnamon-brown to brick-red oval pustules on upper and lower leaf surfaces.",
                            "Apply fungicides if rust appears before tasseling on susceptible lines.",
                            "Select resistant hybrids with Rp genes; early planting to avoid peak spores.",
                            "LOW: Scattered pustules on lower leaves. MEDIUM: Pustules on upper leaves. HIGH: Leaf chlorosis.",
                            "Bio-fungicides like Bacillus subtilis or wettable sulfur.",
                            "Tebuconazole, Propiconazole, or Azoxystrobin.", false),

                    new Disease("corn_healthy", "Healthy Corn Stand", corn, CategoryEnum.UNKNOWN,
                            "Tall, sturdy stalks with broad emerald-green leaves and vigorous ear/tassel development.",
                            "Clean dark green leaves with no necrotic streaks; strong prop roots.",
                            "Provide side-dress nitrogen at V6 and ensure moisture at pollination.",
                            "Maintain weed-free rows and monitor for armyworm.",
                            "LOW: Optimal vitality.",
                            "Organic manure tea and mycorrhizal soil inoculants.",
                            "No chemicals needed.", false),

                    // Apple
                    new Disease("apple_scab", "Apple Scab (Venturia inaequalis)", apple, CategoryEnum.FUNGAL,
                            "Severe fungal disease affecting leaves, blossoms, and fruit.",
                            "Olive-green to velvety dark brown spots on leaves; corky scabby lesions on fruit.",
                            "Rake and destroy fallen leaves in autumn. Spray protective fungicides from bud break.",
                            "Prune trees for open canopy sunlight, choose scab-resistant apple cultivars.",
                            "LOW: Few leaf spots. MEDIUM: 20% leaf infection with fruit scabs. HIGH: Severe defoliation.",
                            "Lime sulfur, wettable sulfur, or potassium bicarbonate.",
                            "Captan, Mancozeb, Difenoconazole, or Cyprodinil.", false),

                    new Disease("apple_black_rot", "Apple Black Rot / Frogeye Spot", apple, CategoryEnum.FUNGAL,
                            "Fungal disease causing frogeye leaf spots, bark cankers, and mummified fruit rot.",
                            "Purple specks expanding into circular spots with tan centers; black mummified apples.",
                            "Prune dead wood and remove mummified apples from tree during winter.",
                            "Maintain tree vigor with balanced fertilizer; paint trunk to prevent sunscald.",
                            "LOW: Few leaf spots. MEDIUM: Cankers on small branches. HIGH: Large trunk cankers.",
                            "Copper sprays during dormant stage.",
                            "Captan, Thiophanate-methyl, or Fludioxonil.", false),

                    new Disease("apple_healthy", "Healthy Apple Foliage", apple, CategoryEnum.UNKNOWN,
                            "Lush green canopy with balanced shoot extension and clean developing fruit.",
                            "Glossy foliage without curling, spots, or powdery coating.",
                            "Ensure regular deep watering and mulch ring around drip line.",
                            "Follow dormant pruning schedule and seasonal pest monitoring.",
                            "LOW: Optimal vitality.",
                            "Compost mulch and organic kelp foliar sprays.",
                            "No chemicals needed.", false),

                    // Grape
                    new Disease("grape_black_rot", "Grape Black Rot (Guignardia bidwellii)", grape, CategoryEnum.FUNGAL,
                            "Devastating fungal disease turning grape berries into hard black mummies.",
                            "Small reddish-brown spots on leaves; berries turn black and shrivel into hard mummies.",
                            "Prune away infected canes and mummified bunches. Apply fungicides early in spring.",
                            "Open canopy via leaf pulling and shoot positioning for excellent airflow.",
                            "LOW: Few leaf spots. MEDIUM: 10-25% bunch infection. HIGH: Total bunch mummification.",
                            "Copper hydroxide and sulfur sprays.",
                            "Myclobutanil, Mancozeb, or Kresoxim-methyl.", false),

                    new Disease("grape_powdery_mildew", "Grape Powdery Mildew (Erysiphe necator)", grape, CategoryEnum.FUNGAL,
                            "Ubiquitous fungal disease coating leaves and berries in a dusty white/gray powdery residue.",
                            "Powdery white or ash-gray fungal coating on upper leaf surfaces; berries split.",
                            "Spray horticultural oil or sulfur at early budbreak through pre-bloom.",
                            "Canopy management for sunlight exposure; avoid excess nitrogen fertilizer.",
                            "LOW: Sparse white patches. MEDIUM: Moderate coating on leaves/clusters. HIGH: Berry splitting.",
                            "Potassium bicarbonate, neem oil, sulfur powder, or Bacillus pumilus.",
                            "Trifloxystrobin, Tebuconazole, or Boscalid.", false),

                    new Disease("grape_healthy", "Healthy Grapevine", grape, CategoryEnum.UNKNOWN,
                            "Clean, vibrant green foliage and well-aerated, uniform fruit clusters.",
                            "Healthy green fan-shaped leaves, firm cane growth, uniform berry development.",
                            "Maintain shoot positioning, suckering, and balanced drip irrigation.",
                            "Monitor cluster zone air circulation and keep vineyard floor weed-free.",
                            "LOW: Optimal vitality.",
                            "Bio-dynamic compost teas and mycorrhizae.",
                            "No chemicals needed.", false),

                    // Rice
                    new Disease("rice_blast", "Rice Blast (Magnaporthe oryzae)", rice, CategoryEnum.FUNGAL,
                            "Destructive cereal disease attacking leaves, collars, nodes, and panicles.",
                            "Spindle-shaped diamond lesions with gray centers and brown borders; neck rot whiteheads.",
                            "Maintain water level in paddy fields. Avoid excess nitrogen. Spray tricyclazole at heading.",
                            "Plant resistant cultivars, use clean seed, practice synchronized planting.",
                            "LOW: Scattered lesions. MEDIUM: Collar and node infection. HIGH: Whitehead sterility.",
                            "Pseudomonas fluorescens seed treatment and bio-fungicides.",
                            "Tricyclazole, Isoprothiolane, or Kasugamycin.", false),

                    new Disease("rice_healthy", "Healthy Rice Crop", rice, CategoryEnum.UNKNOWN,
                            "Vigorous tillering, erect dark green leaves, and uniform golden panicle emergence.",
                            "Clean upright leaves, firm culms, well-filled grains with no spotting.",
                            "Maintain proper water depth and apply split nitrogen-potassium fertilizer.",
                            "Scout for planthoppers and stem borers.",
                            "LOW: Optimal vitality.",
                            "Azospirillum and blue-green algae bio-fertilizers.",
                            "No chemicals needed.", false),

                    // Wheat
                    new Disease("wheat_leaf_rust", "Wheat Leaf Rust (Puccinia triticina)", wheat, CategoryEnum.FUNGAL,
                            "Airborne fungal pathogen producing orange-brown pustules on wheat leaves.",
                            "Small, round-to-oval orange-brown powdery pustules scattered on upper leaf surfaces.",
                            "Apply foliar triazole or strobilurin fungicides at flag leaf emergence if humid.",
                            "Grow rust-resistant wheat varieties, eliminate volunteer wheat seedlings.",
                            "LOW: Pustules on lower leaves. MEDIUM: Pustules on sub-flag leaf. HIGH: Flag leaf covered.",
                            "Bio-formulations of Bacillus subtilis.",
                            "Propiconazole, Tebuconazole, or Azoxystrobin.", false),

                    new Disease("wheat_healthy", "Healthy Wheat Stand", wheat, CategoryEnum.UNKNOWN,
                            "Uniform stand with upright tillers, broad green flag leaves, and well-developed heads.",
                            "Healthy green canopy, absence of lodging, clean stems, plump spikelets.",
                            "Provide timely irrigation during crown root initiation, jointing, and grain filling.",
                            "Monitor soil nutrients and scout for aphids or powdery mildew.",
                            "LOW: Optimal vitality.",
                            "Organic vermicompost extracts.",
                            "No chemicals needed.", false),

                    // General Abiotic Stresses (All Crops)
                    new Disease("nitrogen_deficiency_general", "Nitrogen Deficiency (Abiotic)", null, CategoryEnum.ABIOTIC,
                            "Essential macronutrient shortage causing impaired chlorophyll synthesis and stunted growth.",
                            "Uniform pale green to yellowish discoloration starting on older lower leaves; thin stems.",
                            "Apply quick-acting water-soluble nitrogen fertilizer (urea, ammonium nitrate, or fish hydrolysate).",
                            "Conduct soil tests, incorporate well-rotted compost or leguminous green manure, use split dosing.",
                            "LOW: Mild paling of lowest leaves. MEDIUM: General yellowing of lower canopy. HIGH: Stunting.",
                            "Foliar spray of fish amino acid, liquid kelp, or blood meal soil amendment.",
                            "Calcium Nitrate, Urea (46-0-0), or balanced 20-20-20 water-soluble fertilizer.", true),

                    new Disease("potassium_deficiency_general", "Potassium Deficiency (Abiotic)", null, CategoryEnum.ABIOTIC,
                            "Macronutrient deficiency affecting plant water regulation and stress resistance.",
                            "Marginal chlorosis and scorching (browning/necrosis) along outer edges of older leaves.",
                            "Top-dress with potassium sulfate or spray foliar potassium nitrate solution.",
                            "Maintain balanced N-K ratio in soil, avoid over-applying calcium/magnesium.",
                            "LOW: Slight edge yellowing. MEDIUM: Distinct scorched leaf edges. HIGH: Extensive margin necrosis.",
                            "Wood ash (in moderation), greensand, or kelp meal.",
                            "Potassium Sulfate (0-0-50) or Potassium Nitrate (13-0-44).", true),

                    new Disease("iron_chlorosis_general", "Iron Chlorosis / Interveinal Yellowing", null, CategoryEnum.ABIOTIC,
                            "Micronutrient disorder common in alkaline or waterlogged soils, hindering chlorophyll production.",
                            "Interveinal chlorosis where veins remain dark green while tissue between turns bright yellow, on new leaves.",
                            "Apply chelated iron (Fe-EDDHA for alkaline soil, Fe-EDTA for neutral) as foliar spray or drench.",
                            "Lower soil pH with sulfur if above 7.5; improve soil aeration and avoid over-watering.",
                            "LOW: Light interveinal yellowing on new shoots. MEDIUM: Bright yellow with green veins. HIGH: Ivory leaves.",
                            "Foliar spray of iron sulfate with citric acid or kelp extract.",
                            "Chelated Iron (Fe-EDDHA 6% or Fe-DTPA 7%).", true),

                    new Disease("water_stress_drought_general", "Drought / Underwatering Stress", null, CategoryEnum.ABIOTIC,
                            "Severe soil moisture deficit causing loss of cellular turgor pressure and heat stress.",
                            "Inward leaf curling, midday wilting, dull grayish-green color, dry crispy leaf tips, blossom drop.",
                            "Provide deep, slow soaking irrigation immediately. Mulch soil surface to lock in moisture.",
                            "Install drip irrigation with timers, apply organic mulch 5-8 cm deep, improve soil organic matter.",
                            "LOW: Temporary midday wilt with quick recovery. MEDIUM: Persistent wilt and curl. HIGH: Crispy edges.",
                            "Seaweed extract / biostimulants to reduce osmotic shock.",
                            "Irrigation water + balanced electrolyte/potassium supplements.", true),

                    new Disease("waterlogging_stress_general", "Waterlogging / Root Hypoxia Stress", null, CategoryEnum.ABIOTIC,
                            "Excessive soil saturation depriving roots of oxygen, leading to root rot and nutrient uptake failure.",
                            "Lower leaves turn yellow and drop; limp wilting despite wet soil; foul dark roots; edema blisters.",
                            "Immediately halt irrigation; improve drainage channels; aerate heavy compacted soil around root zone.",
                            "Plant on raised beds, ensure pots have drainage holes, incorporate perlite or compost.",
                            "LOW: Soggy soil with slight yellowing. MEDIUM: Limp foliage and dark roots. HIGH: Root rot collapse.",
                            "Drench soil with diluted Hydrogen Peroxide (3% diluted 1:10) to add oxygen, plus Trichoderma.",
                            "Phosphorous acid (Fosetyl-Al) or Metalaxyl to protect roots from water molds.", true),

                    new Disease("heat_sunscald_stress_general", "Heat Stress & Sunscald", null, CategoryEnum.ABIOTIC,
                            "Excessive solar radiation and high ambient temperatures causing cellular damage and bleaching.",
                            "White, bleached, or papery patches on leaves and exposed fruit; leathery dried spots.",
                            "Erect temporary 30-50% shade cloth; apply kaolin clay foliar spray; maintain root hydration.",
                            "Provide afternoon shade for sensitive crops; maintain healthy dense canopy to shade fruit.",
                            "LOW: Superficial bleaching on upper leaves. MEDIUM: Papery sunscald on 20% fruit. HIGH: Canopy burn.",
                            "Foliar spray of Kaolin clay (Surround WP) and humic acid.",
                            "Anti-transpirant / solar reflectant sprays.", true),

                    new Disease("pest_infestation_general", "Sucking Pest Infestation (Aphids/Mites)", null, CategoryEnum.PEST,
                            "Insect pests piercing plant tissues to feed on sap, stunting growth and transmitting viruses.",
                            "Stippled yellow dots on foliage, fine webbing on leaf undersides, distorted leaves, sticky honeydew.",
                            "Blast foliage with strong water spray; apply insecticidal soap or neem oil directly to leaf undersides.",
                            "Encourage beneficial predators (ladybugs, lacewings); place sticky traps; avoid excess nitrogen.",
                            "LOW: Isolated clusters on young shoots. MEDIUM: Widespread stippling. HIGH: Severe distortion & sooty mold.",
                            "Neem oil (Azadirachtin), insecticidal potassium soap, or diatomaceous earth dusting.",
                            "Acetamiprid, Abamectin, or Spirotetramat.", true),

                    new Disease("unknown_stress_general", "Unidentified Stress / General Advisory", null, CategoryEnum.UNKNOWN,
                            "Diagnostic pattern does not match a single pathogen with high confidence; requires agronomic review.",
                            "Ambiguous chlorosis, subtle leaf curling, localized spots or general lack of plant vigor.",
                            "Conduct agronomic triage: inspect soil moisture, test root firmness, check for stem borers/pests.",
                            "Review watering schedule, check soil pH and EC, avoid over-fertilizing, isolate symptomatic plants.",
                            "LOW: Mild atypical symptoms. MEDIUM: Multiple stress signs. HIGH: Progressive decline.",
                            "Balanced compost tea, organic seaweed extract, and general neem oil preventive spray.",
                            "Do not apply aggressive chemical treatments without lab diagnosis or expert confirmation.", true)
            );

            List<Disease> savedDiseases = diseaseRepository.saveAll(diseases);
            log.info("Seeded {} disease and stress profiles into database.", savedDiseases.size());
        }

        log.info("AgriGuard AI knowledge initialization complete.");
    }
}
