from collections import Counter

def show(items):
  item_stats = dict(Counter(items))

  for item in sorted(item_stats, key=item_stats.get, reverse=True):
    item_count = item_stats[item]
    item_percent = "{:.1f}%".format(item_count * 100 / len(items))
    print(f"{item}, {item_count}, {item_percent}")