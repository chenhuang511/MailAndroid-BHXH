package vn.bhxh.bhxhmail.search;

import android.util.Log;

import java.util.List;

import vn.bhxh.bhxhmail.Account;
import vn.bhxh.bhxhmail.K9;
import vn.bhxh.bhxhmail.mail.Folder;
import vn.bhxh.bhxhmail.mail.MessagingException;
import vn.bhxh.bhxhmail.mailstore.LocalFolder;
import vn.bhxh.bhxhmail.mailstore.LocalStore;


public class SqlQueryBuilder {
    public static void buildWhereClause(Account account, ConditionsTreeNode node,
            StringBuilder query, List<String> selectionArgs) {
        buildWhereClauseInternal(account, node, query, selectionArgs);
    }

    private static void buildWhereClauseInternal(Account account, ConditionsTreeNode node,
            StringBuilder query, List<String> selectionArgs) {
        if (node == null) {
            query.append("1");
            return;
        }

        if (node.mLeft == null && node.mRight == null) {
            SearchSpecification.SearchCondition condition = node.mCondition;
            switch (condition.field) {
                case FOLDER: {
                    String folderName = condition.value;
                    long folderId = getFolderId(account, folderName);
                    if (condition.attribute == SearchSpecification.Attribute.EQUALS) {
                        query.append("folder_id = ?");
                    } else {
                        query.append("folder_id != ?");
                    }
                    selectionArgs.add(Long.toString(folderId));
                    break;
                }
                case SEARCHABLE: {
                    switch (account.getSearchableFolders()) {
                        case ALL: {
                            // Create temporary LocalSearch object so we can use...
                            LocalSearch tempSearch = new LocalSearch();
                            // ...the helper methods in Account to create the necessary conditions
                            // to exclude "unwanted" folders.
                            account.excludeUnwantedFolders(tempSearch);

                            buildWhereClauseInternal(account, tempSearch.getConditions(), query,
                                    selectionArgs);
                            break;
                        }
                        case DISPLAYABLE: {
                            // Create temporary LocalSearch object so we can use...
                            LocalSearch tempSearch = new LocalSearch();
                            // ...the helper methods in Account to create the necessary conditions
                            // to limit the selection to displayable, non-special folders.
                            account.excludeSpecialFolders(tempSearch);
                            account.limitToDisplayableFolders(tempSearch);

                            buildWhereClauseInternal(account, tempSearch.getConditions(), query,
                                    selectionArgs);
                            break;
                        }
                        case NONE: {
                            // Dummy condition, never select
                            query.append("0");
                            break;
                        }
                    }
                    break;
                }
                case MESSAGE_CONTENTS: {
                    String fulltextQueryString = condition.value;
                    if (condition.attribute != SearchSpecification.Attribute.CONTAINS) {
                        Log.e(K9.LOG_TAG, "message contents can only be matched!");
                    }
                    query.append("(EXISTS (SELECT docid FROM messages_fulltext WHERE docid = id AND fulltext MATCH ?))");
                    selectionArgs.add(fulltextQueryString);
                    break;
                }
                default: {
                    appendCondition(condition, query, selectionArgs);
                }
            }
        } else {
            query.append("(");
            buildWhereClauseInternal(account, node.mLeft, query, selectionArgs);
            query.append(") ");
            query.append(node.mValue.name());
            query.append(" (");
            buildWhereClauseInternal(account, node.mRight, query, selectionArgs);
            query.append(")");
        }
    }

    private static void appendCondition(SearchSpecification.SearchCondition condition, StringBuilder query,
                                        List<String> selectionArgs) {
        query.append(getColumnName(condition));
        appendExprRight(condition, query, selectionArgs);
    }

    private static long getFolderId(Account account, String folderName) {
        long folderId = 0;
        try {
            LocalStore localStore = account.getLocalStore();
            LocalFolder folder = localStore.getFolder(folderName);
            folder.open(Folder.OPEN_MODE_RO);
            folderId = folder.getId();
        } catch (MessagingException e) {
            //FIXME
            e.printStackTrace();
        }

        return folderId;
    }

    private static String getColumnName(SearchSpecification.SearchCondition condition) {
        String columnName = null;
        switch (condition.field) {
            case ATTACHMENT_COUNT: {
                columnName = "attachment_count";
                break;
            }
            case BCC: {
                columnName = "bcc_list";
                break;
            }
            case CC: {
                columnName = "cc_list";
                break;
            }
            case DATE: {
                columnName = "date";
                break;
            }
            case DELETED: {
                columnName = "deleted";
                break;
            }
            case FLAG: {
                columnName = "flags";
                break;
            }
            case ID: {
                columnName = "id";
                break;
            }
            case REPLY_TO: {
                columnName = "reply_to_list";
                break;
            }
            case SENDER: {
                columnName = "sender_list";
                break;
            }
            case SUBJECT: {
                columnName = "subject";
                break;
            }
            case TO: {
                columnName = "to_list";
                break;
            }
            case UID: {
                columnName = "uid";
                break;
            }
            case INTEGRATE: {
                columnName = "integrate";
                break;
            }
            case READ: {
                columnName = "read";
                break;
            }
            case FLAGGED: {
                columnName = "flagged";
                break;
            }
            case DISPLAY_CLASS: {
                columnName = "display_class";
                break;
            }
            case THREAD_ID: {
                columnName = "threads.root";
                break;
            }
            case MESSAGE_CONTENTS:
            case FOLDER:
            case SEARCHABLE: {
                // Special cases handled in buildWhereClauseInternal()
                break;
            }
        }

        if (columnName == null) {
            throw new RuntimeException("Unhandled case");
        }

        return columnName;
    }

    private static void appendExprRight(SearchSpecification.SearchCondition condition, StringBuilder query,
                                        List<String> selectionArgs) {
        String value = condition.value;
        SearchSpecification.SearchField field = condition.field;

        query.append(" ");
        String selectionArg = null;
        switch (condition.attribute) {
            case NOT_CONTAINS:
                query.append("NOT ");
                //$FALL-THROUGH$
            case CONTAINS: {
                query.append("LIKE ?");
                selectionArg = "%" + value + "%";
                break;
            }
            case NOT_STARTSWITH:
                query.append("NOT ");
                //$FALL-THROUGH$
            case STARTSWITH: {
                query.append("LIKE ?");
                selectionArg = "%" + value;
                break;
            }
            case NOT_ENDSWITH:
                query.append("NOT ");
                //$FALL-THROUGH$
            case ENDSWITH: {
                query.append("LIKE ?");
                selectionArg = value + "%";
                break;
            }
            case NOT_EQUALS: {
                if (isNumberColumn(field)) {
                    query.append("!= ?");
                } else {
                    query.append("NOT LIKE ?");
                }
                selectionArg = value;
                break;
            }
            case EQUALS: {
                if (isNumberColumn(field)) {
                    query.append("= ?");
                } else {
                    query.append("LIKE ?");
                }
                selectionArg = value;
                break;
            }
        }

        if (selectionArg == null) {
            throw new RuntimeException("Unhandled case");
        }

        selectionArgs.add(selectionArg);
    }

    private static boolean isNumberColumn(SearchSpecification.SearchField field) {
        switch (field) {
            case ATTACHMENT_COUNT:
            case DATE:
            case DELETED:
            case FOLDER:
            case ID:
            case INTEGRATE:
            case THREAD_ID:
            case READ:
            case FLAGGED: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    public static String addPrefixToSelection(String[] columnNames, String prefix, String selection) {
        String result = selection;
        for (String columnName : columnNames) {
            result = result.replaceAll("(?<=^|[^\\.])\\b" + columnName + "\\b", prefix + columnName);
        }

        return result;
    }
}
