package sh.adelessfox.odradek.geometry;

import wtf.reversed.toolbox.collect.Floats;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MeshReader {
    private Accessor indices;
    private Accessor positions;
    private Accessor normals;
    private Accessor tangents;
    private Accessor weights;
    private Accessor bones;
    private final List<Accessor> texCoords = new ArrayList<>();
    private final List<Accessor> colors = new ArrayList<>();

    public MeshReader setIndices(Accessor indices) {
        if (this.indices != null) {
            throw new IllegalStateException("bones already set");
        }
        this.indices = indices;
        return this;
    }

    public MeshReader setPositions(Accessor positions) {
        if (this.positions != null) {
            throw new IllegalStateException("positions already set");
        }
        this.positions = positions;
        return this;
    }

    public MeshReader setNormals(Accessor normals) {
        if (this.normals != null) {
            throw new IllegalStateException("normals already set");
        }
        this.normals = normals;
        return this;
    }

    public MeshReader setTangents(Accessor tangents) {
        if (this.tangents != null) {
            throw new IllegalStateException("tangents already set");
        }
        this.tangents = tangents;
        return this;
    }

    public MeshReader setWeights(Accessor weights) {
        if (this.weights != null) {
            throw new IllegalStateException("weights already set");
        }
        this.weights = weights;
        return this;
    }

    public MeshReader setBones(Accessor bones) {
        if (this.bones != null) {
            throw new IllegalStateException("bones already set");
        }
        this.bones = bones;
        return this;
    }

    public MeshReader addTexCoord(Accessor texCoord) {
        this.texCoords.add(texCoord);
        return this;
    }

    public MeshReader addColor(Accessor color) {
        this.colors.add(color);
        return this;
    }

    public Mesh read() {
        if (indices == null) {
            throw new IllegalStateException("bones not set");
        }
        if (positions == null) {
            throw new IllegalStateException("positions not set");
        }
        if (weights != null && bones == null) {
            throw new IllegalStateException("weights set but bones not set");
        }
        return new Mesh(
            indices.toInts(),
            positions.toFloats(),
            Optional.ofNullable(normals).map(Accessor::toFloats),
            Optional.ofNullable(tangents).map(Accessor::toFloats),
            texCoords.stream().map(Accessor::toFloats).toList(),
            colors.stream().map(Accessor::toBytes).toList(),
            readWeights());
    }

    private Optional<Mesh.Weights> readWeights() {
        if (weights == null && bones == null) {
            return Optional.empty();
        }

        Floats values;
        if (weights != null) {
            if (bones == null) {
                throw new IllegalStateException("weights set but bones not set");
            }
            if (weights.componentCount() != bones.componentCount()) {
                throw new IllegalStateException("weights and bones must have the same number of components");
            }
            if (weights.count() != bones.count()) {
                throw new IllegalStateException("weights and bones must have the same count");
            }
            values = weights.toFloatsNormalized();
        } else {
            // Weights MAY be absent in case there's only one bone per vertex.
            if (bones.componentCount() != 1) {
                throw new IllegalStateException("bones accessor has " + bones.componentCount()
                    + " components, but weights accessor is missing");
            }
            // Build a dummy WEIGHTS accessor with all weights set to 1.0f
            values = Floats.Mutable.allocate(bones.count()).fill(1.0f);
        }

        var indices = bones.toInts();
        var maximumInfluence = bones.componentCount();

        return Optional.of(new Mesh.Weights(values, indices, maximumInfluence));
    }
}
