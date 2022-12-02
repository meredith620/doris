// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

#pragma once

#include "exec/tablet_sink.h"
#include "runtime/row_batch.h"
#include "vec/columns/column.h"

namespace doris {

namespace vectorized {
class VExprContext;
}

namespace stream_load {

class VNodeChannel : public NodeChannel {
public:
    VNodeChannel(OlapTableSink* parent, IndexChannel* index_channel, int64_t node_id);

    ~VNodeChannel() override;

    Status init(RuntimeState* state) override;

    Status open_wait() override;

    Status add_block(vectorized::Block* block,
                     const std::pair<std::unique_ptr<vectorized::IColumn::Selector>,
                                     std::vector<int64_t>>& payload) override;

    int try_send_and_fetch_status(RuntimeState* state,
                                  std::unique_ptr<ThreadPoolToken>& thread_pool_token) override;

    void try_send_block(RuntimeState* state);

    void clear_all_blocks() override;

    // two ways to stop channel:
    // 1. mark_close()->close_wait() PS. close_wait() will block waiting for the last AddBatch rpc response.
    // 2. just cancel()
    void mark_close() override;

protected:
    void _close_check() override;

private:
    std::unique_ptr<vectorized::MutableBlock> _cur_mutable_block;
    PTabletWriterAddBlockRequest _cur_add_block_request;

    using AddBlockReq =
            std::pair<std::unique_ptr<vectorized::MutableBlock>, PTabletWriterAddBlockRequest>;
    std::queue<AddBlockReq> _pending_blocks;
    ReusableClosure<PTabletWriterAddBlockResult>* _add_block_closure = nullptr;
};

class OlapTableSink;

// Write block data to Olap Table.
// When OlapTableSink::open() called, there will be a consumer thread running in the background.
// When you call VOlapTableSink::send(), you will be the producer who products pending batches.
// Join the consumer thread in close().
class VOlapTableSink : public OlapTableSink {
public:
    // Construct from thrift struct which is generated by FE.
    VOlapTableSink(ObjectPool* pool, const RowDescriptor& row_desc,
                   const std::vector<TExpr>& texprs, Status* status);

    ~VOlapTableSink() override;

    Status init(const TDataSink& sink) override;
    // TODO: unify the code of prepare/open/close with result sink
    Status prepare(RuntimeState* state) override;

    Status open(RuntimeState* state) override;

    Status close(RuntimeState* state, Status close_status) override;
    using OlapTableSink::send;
    Status send(RuntimeState* state, vectorized::Block* block, bool eos = false) override;

    size_t get_pending_bytes() const;

private:
    // make input data valid for OLAP table
    // return number of invalid/filtered rows.
    // invalid row number is set in Bitmap
    // set stop_processing if we want to stop the whole process now.
    Status _validate_data(RuntimeState* state, vectorized::Block* block, Bitmap* filter_bitmap,
                          int* filtered_rows, bool* stop_processing);
    Status _validate_column(RuntimeState* state, const TypeDescriptor& type, bool is_nullable,
                            vectorized::ColumnPtr column, size_t slot_index, Bitmap* filter_bitmap,
                            bool* stop_processing, fmt::memory_buffer& error_prefix,
                            vectorized::IColumn::Permutation* rows = nullptr);

    // some output column of output expr may have different nullable property with dest slot desc
    // so here need to do the convert operation
    void _convert_to_dest_desc_block(vectorized::Block* block);

    Status find_tablet(RuntimeState* state, vectorized::Block* block, int row_index,
                       const VOlapTablePartition** partition, uint32_t& tablet_index,
                       bool& stop_processing, bool& is_continue);

    VOlapTablePartitionParam* _vpartition = nullptr;
    std::vector<vectorized::VExprContext*> _output_vexpr_ctxs;
};

} // namespace stream_load
} // namespace doris
