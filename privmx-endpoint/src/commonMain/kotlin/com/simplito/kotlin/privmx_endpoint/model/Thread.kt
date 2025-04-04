//
// PrivMX Endpoint Java.
// Copyright © 2024 Simplito sp. z o.o.
//
// This file is part of the PrivMX Platform (https://privmx.dev).
// This software is Licensed under the MIT License.
//
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.simplito.kotlin.privmx_endpoint.model

/**
 * Holds all available information about a Thread.
 *
 * @param contextId                 ID of the Thread's Context.
 * @param threadId                  ID of the Thread.
 * @param createDate                Thread creation timestamp.
 * @param creator                   ID of the user who created the Thread.
 * @param lastModificationDate      Thread last modification timestamp.
 * @param lastModifier              ID of the user who last modified the Thread.
 * @param users                     List of users (their IDs) with access to the Thread.
 * @param managers                  List of users (their IDs) with management rights.
 * @param version                   Version number (changes on updates).
 * @param lastMsgDate               Timestamp of the last posted message.
 * @param publicMeta                Thread's public metadata.
 * @param privateMeta               Thread's private metadata.
 * @param messagesCount             Total number of messages in the Thread.
 * @param policy                    Thread's policies
 * @param statusCode                Status code of retrieval and decryption of the `Thread`.
 *
 * @category thread
 * @group Thread
 */
class Thread(
    /**
     * ID of the Thread's Context.
     */
    var contextId: String?,
    /**
     * ID of the Thread.
     */
    var threadId: String?,
    /**
     * Thread creation timestamp.
     */
    var createDate: Long?,
    /**
     * ID of the user who created the Thread.
     */
    var creator: String?,
    /**
     * Thread last modification timestamp.
     */
    var lastModificationDate: Long?,
    /**
     * ID of the user who last modified the Thread.
     */
    var lastModifier: String?,
    /**
     * List of users (their IDs) with access to the Thread.
     */
    var users: List<String?>?,
    /**
     * List of users (their IDs) with management rights.
     */
    var managers: List<String?>?,
    /**
     * Version number (changes on updates).
     */
    var version: Long?,
    /**
     * Timestamp of the last posted message.
     */
    var lastMsgDate: Long?,
    /**
     * Thread's public metadata.
     */
    var publicMeta: ByteArray?,
    /**
     * Thread's private metadata.
     */
    var privateMeta: ByteArray?,
    /**
     * Thread's policies
     */
    var policy: ContainerPolicy?,
    /**
     * Total number of messages in the Thread.
     */
    var messagesCount: Long?,
    /**
     * Status code of retrieval and decryption of the `Thread`.
     */
    var statusCode: Long?
)