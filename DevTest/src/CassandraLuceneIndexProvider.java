package com.qlik.service.content.model.titan.index;

import com.google.common.base.Preconditions;

import com.qlik.service.content.model.AbstractModelFactory;
import com.qlik.service.content.model.ListSpecification;
import com.qlik.service.content.model.Model;
import com.qlik.service.content.model.ModelManager;
import com.qlik.service.content.model.ModelObjectPage;
import com.qlik.service.content.model.expression.search.SearchAstFactory;
import com.qlik.service.content.model.expression.search.SearchAstNode;
import com.qlik.service.content.schema.Field;
import com.qlik.service.content.schema.FieldRoles;
import com.qlik.service.content.schema.Schema;
import com.qlik.service.content.schema.Type;
import com.qlik.service.content.schema.qros.QrosSchemaFactory;
import com.qlik.service.content.schema.qros.QrosSchemaWriter;
import com.thinkaurelius.titan.core.Cardinality;
import com.thinkaurelius.titan.diskstorage.BackendException;
import com.thinkaurelius.titan.diskstorage.BaseTransaction;
import com.thinkaurelius.titan.diskstorage.BaseTransactionConfig;
import com.thinkaurelius.titan.diskstorage.BaseTransactionConfigurable;
import com.thinkaurelius.titan.diskstorage.PermanentBackendException;
import com.thinkaurelius.titan.diskstorage.configuration.Configuration;
import com.thinkaurelius.titan.diskstorage.indexing.IndexEntry;
import com.thinkaurelius.titan.diskstorage.indexing.IndexFeatures;
import com.thinkaurelius.titan.diskstorage.indexing.IndexMutation;
import com.thinkaurelius.titan.diskstorage.indexing.IndexProvider;
import com.thinkaurelius.titan.diskstorage.indexing.IndexQuery;
import com.thinkaurelius.titan.diskstorage.indexing.KeyInformation;
import com.thinkaurelius.titan.diskstorage.indexing.RawQuery;
import com.thinkaurelius.titan.graphdb.query.TitanPredicate;

import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class CassandraLuceneIndexProvider implements IndexProvider {

    private final Map<String, Model> indexModels = new HashMap<>();

    public CassandraLuceneIndexProvider(Configuration config) {
    }

    @Override
    public void register(String store, String key, KeyInformation info, BaseTransaction tx)
            throws BackendException {
        if (!(tx instanceof CassandraLuceneTransaction)) {
            throw new IllegalStateException("Unexpected transaction type: " + tx.getClass().getSimpleName());
        }

        CassandraLuceneTransaction clTx = ((CassandraLuceneTransaction) tx);

        IndexModelCreation idxModelCreation = (IndexModelCreation) clTx.getOperation(IndexModelCreation.NAME);
        if (idxModelCreation == null) {
            idxModelCreation = new IndexModelCreation();
            clTx.addOperation(idxModelCreation);
        }

        idxModelCreation.addKey(store, key, info);
    }

    @Override
    public void mutate(Map<String, Map<String, IndexMutation>> mutations,
                       KeyInformation.IndexRetriever informations, BaseTransaction tx) throws BackendException {

        System.out.println("mutate");
        mutations.entrySet().forEach(mutation -> {
            Model model = loadModel(mutation.getKey());
            if (model == null) {
                throw new IllegalStateException(String.format("Failed to load model '%s'", mutation.getKey()));
            }
        });
    }

    @Override
    public void restore(Map<String, Map<String, List<IndexEntry>>> documents,
                        KeyInformation.IndexRetriever informations, BaseTransaction tx) throws BackendException {

        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> query(IndexQuery query, KeyInformation.IndexRetriever informations,
                              BaseTransaction tx) throws BackendException {
        System.out.println("query 1");
        return null;
    }

    @Override
    public Iterable<RawQuery.Result<String>> query(RawQuery query,
                                                   KeyInformation.IndexRetriever informations, BaseTransaction tx) throws BackendException {
        System.out.println("query 2");

        return null;
    }

    @Override
    public BaseTransactionConfigurable beginTransaction(BaseTransactionConfig config)
            throws BackendException {
        return new CassandraLuceneTransaction(config);
    }

    @Override
    public void close() throws BackendException {

    }

    @Override
    public void clearStorage() throws BackendException {
        // TODO drop the search keyspace...
    }

    @Override
    public boolean supports(KeyInformation information, TitanPredicate titanPredicate) {
        return true;
    }

    @Override
    public boolean supports(KeyInformation information) {
        return getSchemaType(information.getDataType()) != null;
    }

    @Override
    public String mapKey2Field(String key, KeyInformation information) {
        Preconditions.checkArgument(!StringUtils.containsAny(key, new char[]{' '}), "Invalid key name provided: %s", key);
        int dotPos = key.indexOf('.');
        if (dotPos == -1) {
            return key;
        }

        return key.substring(dotPos + 1);
    }

    @Override
    public IndexFeatures getFeatures() {
        return null;
    }

    private Model loadModel(String indexName) {
        Model indexModel = indexModels.get(indexName);
        if (indexModel != null) {
            System.out.println("Found index model:" + indexName);
            return indexModel;
        }

        Model schemaModel = ModelManager.getInstance().get("schema");
        ModelObjectPage result = schemaModel.list(new ListSpecification() {
            @Override
            public boolean includeRelationships() {
                return false;
            }

            @Override
            public SearchAstNode getSearchQuery() {
                return SearchAstFactory.getInstance().create("name=" + indexName);
            }
        });

        if (result.isEmpty()) {
            throw new IllegalStateException(String.format("No index model with the name '%s' exists.", indexName));
        }

        if (result.getObjects().size() > 1) {
            throw new IllegalStateException(String.format("Multiple index models with the name '%s' exist.", indexName));
        }

        String qros = (String) result.getObjects().iterator().next().getDataValues().get("qros");
        Schema schema = QrosSchemaFactory.getInstance().create(qros);
        indexModel = AbstractModelFactory.getFactory(schema.getModelType()).create(schema);
        indexModels.put(indexName, indexModel);
        System.out.println("loaded model:" + indexModel.getName());

        return indexModel;
    }

    static Type getSchemaType(Class<?> dataType) {
        if (dataType == Boolean.class) {
            return Type.BOOLEAN;
        }

        if (dataType == Integer.class) {
            return Type.INT;
        }

        if (dataType == Long.class) {
            return Type.LONG;
        }

        if (dataType == String.class) {
            return Type.STRING;
        }

        if (dataType == Date.class) {
            return Type.DATETIME;
        }

        if (dataType == UUID.class) {
            return Type.UUID;
        }

        return null;
    }

    private static String encodeFieldName(String fieldName) {
        return fieldName.replace(".", "_");
    }

    class CassandraLuceneTransaction implements BaseTransactionConfigurable {

        private final BaseTransactionConfig config;

        private final Map<String, TransactionOperation> operations = new LinkedHashMap<>();

        private CassandraLuceneTransaction(BaseTransactionConfig config) {
            this.config = config;
        }

        TransactionOperation getOperation(String operationName) {
            return operations.get(operationName);
        }


        void addOperation(TransactionOperation operation) {
            if (operations.put(operation.getName(), operation) != null) {
                throw new IllegalStateException("Duplicate operation added: " + operation.getName());
            }
        }

        @Override
        public void commit() throws BackendException {
            try {
                operations.values().forEach(TransactionOperation::commit);

            } catch (Throwable e) {
                throw new PermanentBackendException(e);

            } finally {
                close();

            }
        }

        @Override
        public void rollback() throws BackendException {
            close();
        }

        private void close() throws BackendException {
        }

        @Override
        public BaseTransactionConfig getConfiguration() {
            return config;
        }
    }

    interface TransactionOperation {
        String getName();

        void commit();
    }

    class IndexModelCreation implements TransactionOperation {
        static final String NAME = "indexModelCreation";

        private final Map<String, Map<String, Field>> indexFieldsByName = new LinkedHashMap<>();

        @Override
        public String getName() {
            return NAME;
        }

        void addKey(String indexName, String key, KeyInformation info) {
            Map<String, Field> fields = indexFieldsByName.get(indexName);
            if (fields == null) {
                fields = new LinkedHashMap<>();
                indexFieldsByName.put(indexName, fields);
            }

            Type type = getSchemaType(info.getDataType());
            if (type == null) {
                throw new IllegalStateException("Unsupported index data type: " + info.getDataType().getSimpleName());
            }

            String fieldName = encodeFieldName(key);
            Field field = new Field() {
                @Override
                public String getName() {
                    return fieldName;
                }

                @Override
                public Type getType() {
                    return type;
                }

                @Override
                public boolean isCollection() {
                    return info.getCardinality() != Cardinality.SINGLE;
                }
            };

            fields.put(key, field);
        }

        @Override
        public void commit() {
            if (indexFieldsByName.isEmpty()) {
                return;
            }

            System.out.println("Creating index: " + indexFieldsByName.entrySet().iterator().next().getKey());
            indexFieldsByName.entrySet().forEach(indexDef -> {
                Schema indexSchema = createIndexSchema(indexDef);
                persistIndexSchema(indexSchema);

                Model indexModel = AbstractModelFactory.getFactory(indexSchema.getModelType()).create(indexSchema);
                indexModels.put(indexSchema.getName(), indexModel);
            });
        }

        private void persistIndexSchema(Schema indexSchema) {
            Map<String, Object> schemaAttributes = new HashMap<>();
            schemaAttributes.put("name", indexSchema.getName());
            schemaAttributes.put("qros", new QrosSchemaWriter().toString(indexSchema));

            Model schemaModel = ModelManager.getInstance().get("schema");
            schemaModel.create(schemaAttributes);
        }

        private Schema createIndexSchema(final Map.Entry<String, Map<String, Field>> indexDef) {
            List<Field> uuidFields = indexDef.getValue().values().stream().filter(field -> field.getType() == Type.UUID).collect(Collectors.toList());
            if (uuidFields.isEmpty()) {
                throw new IllegalStateException("A single UUID field is requires in the index.");
            }

            if (uuidFields.size() > 1) {
                throw new IllegalStateException("Multiple UUID fields are not supported in an index.");
            }

            return new Schema() {
                // TODO
                private final String name = Character.toUpperCase(indexDef.getKey().charAt(0)) + indexDef.getKey().substring(1);

                private final Map<String, Field> fields = indexDef.getValue();

                private final FieldRoles fieldRoles = new FieldRoles() {
                    @Override
                    public List<Field> getIdentityFields() {
                        return uuidFields;
                    }

                    @Override
                    public Collection<Field> getSearchFields() {
                        return fields.values();
                    }
                };

                @Override
                public String getModelType() {
                    return "cassandra";
                }

                @Override
                public String getName() {
                    return name;
                }

                @Override
                public UUID getId() {
                    return UUID.randomUUID();
                }

                @Override
                public Collection<Field> getFields() {
                    return fields.values();
                }

                @Override
                public Field getField(String name) {
                    return fields.get(name);
                }

                @Override
                public FieldRoles getFieldRoles() {
                    return fieldRoles;
                }

                @Override
                public Collection<String> getSearchFieldNames() {
                    return fields.keySet();
                }

                @Override
                public boolean isPrivate() {
                    return true;
                }
            };
        }
    }
}

