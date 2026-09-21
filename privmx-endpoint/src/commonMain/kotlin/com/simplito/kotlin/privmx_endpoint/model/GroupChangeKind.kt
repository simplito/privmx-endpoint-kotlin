package com.simplito.kotlin.privmx_endpoint.model

enum class GroupChangeKind {
    CREATED,
    PUBLIC_META_UPDATED,
    PRIVATE_META_UPDATED,
    POLICY_UPDATED,
    KEY_ROTATED,
    MEMBER_ADDED,
    MEMBER_REMOVED,
    ERA_CUT,
    ARCHIVE_PRUNED
}