//
// PrivMX Endpoint Kotlin.
// Copyright © 2025 Simplito sp. z o.o.
//
// This file is part of the PrivMX Platform (https://privmx.dev).
// This software is Licensed under the MIT License.
//
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.simplito.kotlin.privmx_endpoint.model

/**
 * Contains results of listing methods.
 *
 * @param T type of items stored in list.
 * @property totalAvailable    Total items available to get
 * @property readItems         List of items read during single method call
*/
class PagingList<T>(val totalAvailable: Long?, val readItems: List<T>)