package com.matyrobbrt.simpleminers.results;

import com.matyrobbrt.simpleminers.SimpleMiners;
import com.matyrobbrt.simpleminers.results.modifier.ResultModifier;
import com.matyrobbrt.simpleminers.results.predicate.ResultPredicate;
import com.matyrobbrt.simpleminers.util.SupplyingMutable;
import com.matyrobbrt.simpleminers.util.Utils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public final class ResultSet implements Supplier<List<ItemResult>> {
    public static final ResourceKey<Registry<ResultSet>> RESULTS_REGISTRY = SimpleMiners.registryKey("results");
    public static final SupplyingMutable<IForgeRegistry<ResultSet>> REGISTRY = new SupplyingMutable<>();

    @Nullable
    private final String requiredMod;
    private final String minerType;
    private final List<ItemResult> results;
    private final List<Copying> copying;

    private List<ItemResult> immutableResults;

    public static final Codec<ResultSet> CODEC = RecordCodecBuilder.create(in -> in.group(
            Codec.STRING.fieldOf("minerType").forGetter(ResultSet::minerType),
            ItemResult.CODEC_LIST.optionalFieldOf("results", List.of()).forGetter(it -> it.results),
            Copying.LIST_CODEC.optionalFieldOf("copying", List.of()).forGetter(it -> it.copying)
    ).apply(in, ResultSet::new));

    public static final Codec<ResultSet> REQUIRED_MOD_AWARE_CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<ResultSet, T>> decode(DynamicOps<T> ops, T input) {
            return ops.getMap(input).flatMap(map -> {
                T modId = map.get("requiredMod");
                if (modId == null) modId = ops.createString("");
                return ops.getStringValue(modId).flatMap(mod -> {
                    if (!mod.isBlank() && !ModList.get().isLoaded(mod)) {
                        return ops.getStringValue(map.get("minerType"))
                                .map(type -> new ResultSet(type, List.of(), List.of(), mod))
                                .map(it -> Pair.of(it, ops.empty()));
                    } else {
                        return CODEC.decode(ops, input);
                    }
                });
            });
        }

        @Override
        public <T> DataResult<T> encode(ResultSet input, DynamicOps<T> ops, T prefix) {
            return CODEC.encode(input, ops, prefix)
                    .flatMap(map -> {
                        if (input.requiredMod == null) return DataResult.success(map);
                        return ops.mergeToMap(map, ops.createString("requiredMod"), ops.createString(input.requiredMod));
                    });
        }
    };

    public ResultSet(String minerType, List<ItemResult> results, List<Copying> copying) {
        this.minerType = minerType;
        this.results = results;
        this.copying = copying;
        this.requiredMod = null;
    }

    public ResultSet(String minerType, List<ItemResult> results, List<Copying> copying, String modId) {
        this.minerType = minerType;
        this.results = results;
        this.copying = copying;
        this.requiredMod = (modId == null || modId.isBlank()) ? null : modId;
    }

    public String minerType() {
        return minerType;
    }

    @Override
    public List<ItemResult> get() {
        if (immutableResults == null) {
            immutableResults = new ArrayList<>();
            immutableResults.addAll(results);

            for (final var copying : this.copying) {
                copying.results().forEach(result -> {
                    final var copy = result.mutableCopy();
                    copying.modifier.ifPresent(copy::and);
                    copying.predicate.ifPresent(copy::and);
                    immutableResults.add(copy.freeze());
                });
            }
        }
        return immutableResults;
    }

    public record Copying(Optional<ResultModifier> modifier, Optional<ResultPredicate> predicate, List<IItemResult> results) {
        public static final Codec<Copying> CODEC = RecordCodecBuilder.create(in -> in.group(
                ResultModifier.LIST_DIRECT_CODEC.optionalFieldOf("modifier").forGetter(Copying::modifier),
                ResultPredicate.LIST_DIRECT_CODEC.optionalFieldOf("predicate").forGetter(Copying::predicate),
                IItemResult.CODEC_LIST.fieldOf("results").forGetter(Copying::results)
        ).apply(in, Copying::new));
        public static final Codec<List<Copying>> LIST_CODEC = Utils.singleOrListOf(CODEC);
    }

    record RequiredMod(Optional<String> modId) {}
}
